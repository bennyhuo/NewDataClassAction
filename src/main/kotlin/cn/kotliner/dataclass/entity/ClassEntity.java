package cn.kotliner.dataclass.entity;

import org.jetbrains.kotlin.psi.KtClass;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dim on 2015/7/15.
 */
public class ClassEntity {

    private KtClass ktClass;
    private String fieldTypeSuffix;
    private List<FieldEntity> fields = new ArrayList<>();
    private List<ClassEntity> innerClasss = new ArrayList<>();
    private String packName;
    private String className;
    /**
     * 存储 comment
     */
    private String extra;
    private boolean generate = true;
    private boolean lock = false;

    public String getPackName() {
        return packName;
    }

    public void setPackName(String packName) {
        this.packName = packName;
    }

    public String getClassName() {
        return ktClass == null ? className : ktClass.getName();
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public boolean isLock() {
        return lock;
    }

    public void setLock(boolean lock) {
        this.lock = lock;
    }

    public boolean isGenerate() {
        return generate;
    }

    public void setGenerate(boolean generate) {
        this.generate = generate;
    }

    public String getExtra() {
        return extra;
    }

    public void setExtra(String extra) {
        this.extra = extra;
    }

    public void addAllFields(List fields) {
        this.fields.addAll(fields);
    }

    public void addField(FieldEntity fieldEntity) {
        this.fields.add(fieldEntity);
    }

    public void addInnerClass(ClassEntity classEntity) {
        this.innerClasss.add(classEntity);
    }

    public List<ClassEntity> getInnerClasss() {
        return innerClasss;
    }

    public String getFieldTypeSuffix() {
        return fieldTypeSuffix;
    }

    public void setFieldTypeSuffix(String fieldTypeSuffix) {
        this.fieldTypeSuffix = fieldTypeSuffix;
    }

    public List<? extends FieldEntity> getFields() {
        return fields;
    }



    public String getQualifiedName() {
        return ktClass.getFqName().asString();
    }

    public boolean isSame(JSONObject o) {
        if (o == null) {
            return false;
        }
        boolean same = true;
        for (String key : o.keySet()) {
            same = false;
            for (FieldEntity field : fields) {
                if (field.getKey().equals(key)) {
                    if (field.isSameType(o.get(key))) {
                        same = true;
                    }
                    break;
                }
            }
            if (!same) {
                break;
            }
        }
        return same;
    }

    public KtClass getKtClass() {
        return ktClass;
    }

    public void setKtClass(KtClass ktClass) {
        this.ktClass = ktClass;
    }
}
