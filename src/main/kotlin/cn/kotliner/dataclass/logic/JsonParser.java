package cn.kotliner.dataclass.logic;

import cn.kotliner.dataclass.common.CheckUtil;
import cn.kotliner.dataclass.common.StringUtils;
import cn.kotliner.dataclass.common.Utils;
import cn.kotliner.dataclass.config.Config;
import cn.kotliner.dataclass.entity.ClassEntity;
import cn.kotliner.dataclass.entity.FieldEntity;
import cn.kotliner.dataclass.entity.IterableFieldEntity;
import cn.kotliner.dataclass.entity.JsonDataType;
import com.google.gson.JsonSyntaxException;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.Project;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.intellij.psi.codeStyle.CodeStyleSettingsManager;
import com.intellij.psi.codeStyle.CommonCodeStyleSettings;
import org.apache.http.util.TextUtils;
import org.jetbrains.kotlin.idea.KotlinLanguage;
import org.jetbrains.kotlin.psi.*;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created by benny on 7/1/17.
 */
public class JsonParser {

    private final KtPsiFactory factory;
    private final KtFile file;
    private final Project project;
    private ClassEntity generateClassEntity;
    private String jsonString;
    private KtClass ktClass;

    public JsonParser(String jsonString, KtClass ktClass) {
        this.jsonString = jsonString;
        this.ktClass = ktClass;
        this.file = ktClass.getContainingKtFile();
        this.project = ktClass.getProject();
        this.factory = KtPsiFactoryKt.KtPsiFactory(project);
    }

    public void parse(){
        JSONObject jsonObject = null;
        try {
            jsonObject = parseJSONObject(jsonString);
            generateClassEntity = new ClassEntity();
            generateClassEntity.setKtClass(ktClass);
            parseJson(jsonObject);

            WriteCommandAction.runWriteCommandAction(project, new Runnable() {
                @Override
                public void run() {
                    KtParameterList ktParameterList = ktClass.getPrimaryConstructor().getValueParameterList();

                    for (FieldEntity fieldEntity : generateClassEntity.getFields()) {
                        ktParameterList.addParameter(factory.createParameter("\nvar " + fieldEntity.getFieldName() + ": " + fieldEntity.getBriefType()));
                    }

                    for (ClassEntity entity : generateClassEntity.getInnerClasss()) {
                        processInnerClassEntities(ktClass, entity);
                    }

                    CodeStyleSettings settings = CodeStyleSettingsManager.getSettings(project);
                    CommonCodeStyleSettings jetCommonSettings = settings.getCommonSettings(KotlinLanguage.INSTANCE);
                    int wrapStrategy = jetCommonSettings.METHOD_PARAMETERS_WRAP;
                    jetCommonSettings.METHOD_PARAMETERS_WRAP = CommonCodeStyleSettings.WRAP_ALWAYS;
                    CodeStyleManager codeStyleManager = CodeStyleManager.getInstance(project);
                    codeStyleManager.reformat(file);
                    jetCommonSettings.METHOD_PARAMETERS_WRAP = wrapStrategy;
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void processInnerClassEntities(KtClass parent, ClassEntity classEntity) {
        KtClass innerClass = factory.createClass("data class " + classEntity.getClassName() + "()");
        KtParameterList ktParameterList = innerClass.getPrimaryConstructor().getValueParameterList();

        for (FieldEntity fieldEntity : classEntity.getFields()) {
            ktParameterList.addParameter(factory.createParameter("\nvar " + fieldEntity.getFieldName() + ": " + fieldEntity.getBriefType()));
        }
        KtClassBody classBody = KtClassOrObjectKt.getOrCreateBody(parent);
        //注意这个赋值是必要的，返回的是添加成功的，传入的已经废掉了。
        innerClass = (KtClass) classBody.addBefore(innerClass, classBody.getRBrace());

        for (ClassEntity entity : classEntity.getInnerClasss()) {
            processInnerClassEntities(innerClass, entity);
        }
    }


    private List<FieldEntity> createFields(JSONObject json, List<String> fieldList, ClassEntity parentClass) {

        List<FieldEntity> fieldEntityList = new ArrayList<FieldEntity>();
        List<String> listEntityList = new ArrayList<String>();
        boolean writeExtra = Config.getInstant().isGenerateComments();

        for (int i = 0; i < fieldList.size(); i++) {
            String key = fieldList.get(i);
            Object value = json.get(key);
            if (value instanceof JSONArray) {
                listEntityList.add(key);
                continue;
            }
            FieldEntity fieldEntity = createField(parentClass, key, value);
            fieldEntityList.add(fieldEntity);
            if (writeExtra) {
                writeExtra = false;
                parentClass.setExtra(Utils.createCommentString(json, fieldList));
            }
        }

        for (int i = 0; i < listEntityList.size(); i++) {
            String key = listEntityList.get(i);
            Object type = json.get(key);
            FieldEntity fieldEntity = createField(parentClass, key, type);
            fieldEntityList.add(fieldEntity);
        }

        return fieldEntityList;
    }

    private FieldEntity createField(ClassEntity parentClass, String key, Object type) {
        //过滤 不符合规则的key
        String fieldName = CheckUtil.getInstant().handleArg(key);
        if (Config.getInstant().isUseSerializedName()) {
            fieldName = StringUtils.captureStringLeaveUnderscore(convertSerializedName(fieldName));
        }
        FieldEntity fieldEntity = typeByValue(parentClass, key, type);
        fieldEntity.setFieldName(fieldName);
        return fieldEntity;
    }

    private FieldEntity typeByValue(ClassEntity parentClass, String key, Object type) {
        FieldEntity result;
        if (type instanceof JSONObject) {
            FieldEntity fieldEntity = new FieldEntity();
            ClassEntity innerClassEntity = createInnerClass(createSubClassName(key, type), (JSONObject) type, parentClass);
            fieldEntity.setKey(key);
            fieldEntity.setTargetClass(innerClassEntity);
            result = fieldEntity;
        } else if (type instanceof JSONArray) {
            result = handleJSONArray(parentClass, (JSONArray) type, key, 1);
        } else {
            FieldEntity fieldEntity = new FieldEntity();
            fieldEntity.setKey(key);
            fieldEntity.setType(JsonDataType.typeOfObject(type).getValue());
            result = fieldEntity;
            if (type != null) {
                result.setValue(type.toString());
            }
        }
        result.setKey(key);
        return result;
    }

    private FieldEntity handleJSONArray(ClassEntity parentClass, JSONArray jsonArray, String key, int deep) {

        FieldEntity fieldEntity;
        if (jsonArray.length() > 0) {
            Object item = jsonArray.get(0);
            if (item instanceof JSONObject) {
                item = getJsonObject(jsonArray);
            }
            fieldEntity = listTypeByValue(parentClass, key, item, deep);
        } else {
            fieldEntity = new IterableFieldEntity();
            fieldEntity.setKey(key);
            fieldEntity.setType("?");
            ((IterableFieldEntity) fieldEntity).setDeep(deep);
        }
        return fieldEntity;
    }

    private FieldEntity listTypeByValue(ClassEntity parentClass, String key, Object type, int deep) {

        FieldEntity item = null;
        if (type instanceof JSONObject) {
            IterableFieldEntity iterableFieldEntity = new IterableFieldEntity();
            ClassEntity innerClassEntity = createInnerClass(createSubClassName(key, type), (JSONObject) type, parentClass);
            iterableFieldEntity.setKey(key);
            iterableFieldEntity.setDeep(deep);
            iterableFieldEntity.setTargetClass(innerClassEntity);
            item = iterableFieldEntity;
        } else if (type instanceof JSONArray) {
            FieldEntity fieldEntity = handleJSONArray(parentClass, (JSONArray) type, key, ++deep);
            fieldEntity.setKey(key);
            item = fieldEntity;
        } else {
            IterableFieldEntity fieldEntity = new IterableFieldEntity();
            fieldEntity.setKey(key);
            fieldEntity.setType(type.getClass().getSimpleName());
            fieldEntity.setDeep(deep);
            item = fieldEntity;
        }
        return item;
    }

    private String createSubClassName(String key, Object o) {
        String name = "";
        if (o instanceof JSONObject) {
            if (TextUtils.isEmpty(key)) {
                return key;
            }
            String[] strings = key.split("_");
            StringBuilder stringBuilder = new StringBuilder();
            for (int i = 0; i < strings.length; i++) {
                stringBuilder.append(StringUtils.captureName(strings[i]));
            }
            name = stringBuilder.toString();
        }
        return name;

    }


    /**
     * @param className
     * @param json
     * @param parentClass
     * @return
     */
    private ClassEntity createInnerClass(String className, JSONObject json, ClassEntity parentClass) {
        ClassEntity subClassEntity = new ClassEntity();
        Set<String> set = json.keySet();
        List<String> list = new ArrayList<String>(set);
        List<FieldEntity> fields = createFields(json, list, subClassEntity);
        subClassEntity.addAllFields(fields);
        subClassEntity.setClassName(className);
        parentClass.addInnerClass(subClassEntity);
        return subClassEntity;
    }


    private String convertSerializedName(String fieldName) {
        if (Config.getInstant().isUseFieldNamePrefix() &&
                !TextUtils.isEmpty(Config.getInstant().getFiledNamePreFixStr())) {
            fieldName = Config.getInstant().getFiledNamePreFixStr() + "_" + fieldName;
        }
        return fieldName;
    }

    private void parseJson(JSONObject json) {
        List<String> fieldList = collectGenerateFiled(json);
        generateClassEntity.addAllFields(createFields(json, fieldList, generateClassEntity));
    }

    private List<String> collectGenerateFiled(JSONObject json) {
        Set<String> keySet = json.keySet();
        List<String> fieldList = new ArrayList<String>();
        for (String key : keySet) {
            fieldList.add(key);
        }
        return fieldList;
    }

    private JSONObject parseJSONObject(String jsonStr) {
        if (jsonStr.startsWith("{")) {
            return new JSONObject(jsonStr);
        } else if (jsonStr.startsWith("[")) {
            JSONArray jsonArray = new JSONArray(jsonStr);
            if (jsonArray.length() > 0 && jsonArray.get(0) instanceof JSONObject) {
                return getJsonObject(jsonArray);
            }
        }
        throw new JsonSyntaxException("Neither a JsonObject nor a JsonArray.");
    }

    private JSONObject getJsonObject(JSONArray jsonArray) {
        JSONObject resultJSON = jsonArray.getJSONObject(0);

        for (int i = 1; i < jsonArray.length(); i++) {
            Object value = jsonArray.get(i);
            if (!(value instanceof JSONObject)) {
                break;
            }
            JSONObject json = (JSONObject) value;
            for (String key : json.keySet()) {
                if (!resultJSON.keySet().contains(key)) {
                    resultJSON.put(key, json.get(key));
                }
            }
        }
        return resultJSON;
    }

    /**
     * 过滤掉// 和/** 注释
     *
     * @param str
     * @return
     */
    public String removeComment(String str) {
        String temp = str.replaceAll("/\\*" +
                "[\\S\\s]*?" +
                "\\*/", "");
        return temp.replaceAll("//[\\S\\s]*?\n", "");
    }
    //endregion
}
