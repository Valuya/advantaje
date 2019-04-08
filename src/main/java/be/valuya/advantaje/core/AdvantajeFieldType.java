package be.valuya.advantaje.core;

import java.util.stream.Stream;

public enum AdvantajeFieldType {

    LOGICAL(1),
    NUMERIC(2),
    DATE(3),
    STRING(4),
    MEMO(5),
    BINARY(6),
    IMAGE(7),
    DOUBLE(10),
    INTEGER(11),
    SHORTINT(12),
    TIME(13),
    TIMESTAMP(14),
    AUTOINC(15),
    RAW(16),
    CURRENCY(17), // DOUBLE
    MONEY(18) // DECIMAL
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
