package be.valuya.advantaje.core;

import java.util.Optional;

public class AdvantajeValue<T> {

    private AdvantajeField<? extends T> field;
    private Optional<T> valueOptional;

    public AdvantajeValue(AdvantajeField<? extends T> field, Optional<T> valueOptional) {
        this.field = field;
        this.valueOptional = valueOptional;
    }

    public AdvantajeField<? extends T> getField() {
        return field;
    }

    public void setField(AdvantajeField<T> field) {
        this.field = field;
    }

    public Optional<T> getValueOptional() {
        return valueOptional;
    }

    public void setValueOptional(Optional<T> valueOptional) {
        this.valueOptional = valueOptional;
    }
}
