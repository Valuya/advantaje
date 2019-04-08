package be.valuya.advantaje.core;

public class AdvantajeField {

    private String name;
    private AdvantajeFieldType fieldType;
    private int length;

    public AdvantajeField(String name, AdvantajeFieldType fieldType) {
        this.name = name;
        this.fieldType = fieldType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public AdvantajeFieldType getFieldType() {
        return fieldType;
    }

    public void setFieldType(AdvantajeFieldType fieldType) {
        this.fieldType = fieldType;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    @Override
    public String toString() {
        return "AdvantajeField{" +
                "name='" + name + '\'' +
                '}';
    }
}
