package be.valuya.advantaje.core;

import java.util.List;

public class AdvantajeRecord {

    private final List<? extends AdvantajeValue<?>> values;

    public AdvantajeRecord(List<? extends AdvantajeValue<?>> values) {
        this.values = values;
    }

    public List<? extends AdvantajeValue<?>> getValues() {
        return values;
    }
}
