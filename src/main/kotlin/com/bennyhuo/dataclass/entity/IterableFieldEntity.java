package com.bennyhuo.dataclass.entity;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Created by dim on 16/11/6.
 */
public class IterableFieldEntity extends FieldEntity {

    private static final String listTemplate = "java.util.List<%s>";
    private static final String briefListTemplate = "List<%s>";

    private int deep;

    public int getDeep() {
        return deep;
    }

    public void setDeep(int deep) {
        this.deep = deep;
    }

    public String getRealType() {
        String typeName = getClassTypeName();
        return String.format(getBriefTypeReg(), typeName);
    }

    public String getBriefType() {
        return String.format(getBriefTypeReg(), getBriefClassTypeName());
    }

    private String getClassTypeName() {
        String typeName = "";
        if (targetClass != null) {
            typeName = targetClass.getClassName();
        } else if (getType() != null && getType().length() > 0) {
            typeName = getType();
        }
        return typeName;
    }

    private String getBriefClassTypeName() {
        String typeName = "";
        if (targetClass != null) {
            typeName = targetClass.getClassName();
        } else if (getType() != null && getType().length() > 0) {
            typeName = getType();
        }
        return typeName;
    }


    public String getFullNameType() {
        String typeName = getClassTypeName();
        return String.format(getTypeReg(), typeName);
    }

    private String getTypeReg() {
        return getRegForDeep(listTemplate);
    }

    private String getBriefTypeReg() {
        return getRegForDeep(briefListTemplate);
    }

    private String getRegForDeep(String template) {
        if (deep > 0) {
            String format = template;
            for (int i = 1; i < deep; i++) {
                format = String.format(format, template);
            }
            return format;
        }
        return "%s";
    }

    @Override
    public boolean isSameType(Object o) {

        if (o instanceof JSONArray) {
            Object o1 = deepObjectFromJson(deep, 0, (JSONArray) o);
            if (o1 instanceof JSONObject && getTargetClass() != null) {
                return getTargetClass().isSame((JSONObject) o1);
            } else if (getTargetClass() == null && getType() != null) {
                return JsonDataType.isSameDataType(JsonDataType.typeOfString(getType()), JsonDataType.typeOfObject(o1));
            }
        } else {
            return false;
        }

        return super.isSameType(o);
    }


    private Object deepObjectFromJson(int deep, int current, JSONArray array) {
        if (deep <= current) {
            return null;
        }
        if (array.length() > 0) {
            if (deep == current + 1 && !(array.get(0) instanceof JSONArray)) {
                return array.get(0);
            }
            if (array.get(0) instanceof JSONArray) {
                return deepObjectFromJson(deep, ++current, array.getJSONArray(0));
            }
        }
        return null;
    }
}
