package be.valuya.advantaje.core;

import java.util.List;

public class AdvantajeTableMetaData {

    private final int recordCount;
    private List<AdvantajeField<?>> fields;

    public AdvantajeTableMetaData(int recordCount, List<AdvantajeField<?>> fields) {
        this.recordCount = recordCount;
        this.fields = fields;
    }

    public int getRecordCount() {
        return recordCount;
    }

    public List<AdvantajeField<?>> getFields() {
        return fields;
    }

}
