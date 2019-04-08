package be.valuya.advantaje.core;

import java.util.stream.Stream;

public enum AdvantajeFieldType {

    STRING(4),
    INTEGER(11),
    DATE(3),
    TIMESTAMP(14),
    LOGICAL(1),
    CURRENCY(17)
    ;

    private final int code;

    AdvantajeFieldType(int code) {
        this.code = code;
    }

    public static AdvantajeFieldType fromCode(int code) {
        return Stream.of(values())
                .filter(field -> field.hasCode(code))
                .findFirst()
                .orElseThrow(() -> getIllegalCodeError(code));
    }

    private boolean hasCode(int code) {
        return this.code == code;
    }

    private static IllegalArgumentException getIllegalCodeError(int code) {
        return new IllegalArgumentException("Invalid code for AdvantageFieldType: " + code);
    }
}
