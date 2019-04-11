package be.valuya.advantaje.core;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public class AdvantajeRecord {

    private final Map<String, AdvantajeValue<?>> valueMap = new LinkedHashMap<>();

    public <T> T getValue(String key) {
        return (T) getValueOptional(key)
                .orElseThrow(() -> new AdvantageException("No value for this field: " + key));
    }

    public <T> Optional<T> getValueOptional(String key) {
        AdvantajeValue<?> advantajeValueNullable = valueMap.get(key);
        AdvantajeValue<?> advantajeValue = Optional.ofNullable(advantajeValueNullable)
                .orElseThrow(IllegalArgumentException::new);
        return (Optional<T>) advantajeValue.getValueOptional();
    }

    public <T> void put(AdvantajeValue<T> advantajeValue) {
        AdvantajeField<? extends T> field = advantajeValue.getField();
        String fieldName = field.getName();
        valueMap.put(fieldName, advantajeValue);
    }

    public Map<String, AdvantajeValue<?>> getValueMap() {
        return valueMap;
    }
}
