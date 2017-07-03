package cn.kotliner.dataclass.entity;

import cn.kotliner.dataclass.common.CheckUtil;
import org.apache.http.util.TextUtils;
import org.json.JSONObject;

/**
 * Created by dim on 2015/7/15.
 */
public class FieldEntity {

    protected String key;
    protected String type; //类型
    protected String fieldName; // 生成的名字
    protected String value; // 值
    protected ClassEntity targetClass; //依赖的实体类
    protected boolean generate = true;

    public ClassEntity getTargetClass() {
        return targetClass;
    }

    public void setTargetClass(ClassEntity targetClass) {
        this.targetClass = targetClass;
    }

    public boolean isGenerate() {
        return generate;
    }

    public void setGenerate(boolean generate) {
        this.generate = generate;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        if (TextUtils.isEmpty(fieldName)) {
            return;
        }
        this.fieldName = fieldName;
    }

    public String getGenerateFieldName() {
        return CheckUtil.INSTANCE.formatIdentifier(fieldName);
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        //回头再重新理一下
        if (type.equals("Integer")) {
            this.type = "Int";
        } else {
            this.type = type;
        }
    }

    public String getRealType() {
        if (targetClass != null) {
            return targetClass.getClassName();
        }
        return type;
    }

    public String getBriefType() {
        if (targetClass != null) {
            return targetClass.getClassName();
        }
        int i = type.indexOf(".");
        if (i > 0) {
            return type.substring(i);
        }
        return type;
    }

    public String getFullNameType() {
        if (targetClass != null) {
            return targetClass.getQualifiedName();
        }
        return type;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public boolean isSameType(Object o) {
        if (o instanceof JSONObject) {
            if (targetClass != null) {
                return targetClass.isSame((JSONObject) o);
            }
        } else {
            return JsonDataType.isSameDataType(JsonDataType.typeOfString(type), JsonDataType.typeOfObject(o));
        }
        return false;
    }
}
