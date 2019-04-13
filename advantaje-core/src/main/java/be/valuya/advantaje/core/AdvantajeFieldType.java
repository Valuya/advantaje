package be.valuya.advantaje.core;

import java.util.stream.Stream;

/**
 * http://devzone.advantagedatabase.com/dz/webhelp/advantage7.1/mergedProjects/ads-dotnet/base_ado_net_doc/advantage_net_data_provider_data_types.htm
 * http://devzone.advantagedatabase.com/dz/webhelp/advantage7.1/server1/adt_field_types_and_specifications.htm
 * http://devzone.advantagedatabase.com/dz/webhelp/Advantage11.1/index.html?dotnet_advantage_net_data_provider_data_types.htm
 * http://devzone.advantagedatabase.com/dz/webhelp/Advantage11.1/index.html?master_adt_field_types_and_specifications.htm
 */
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
    MONEY(18),// DECIMAL
    /**
     * Case insensitive fixed-length character field that is stored entirely in the table.
     */
    CISSTRING(20), // String
    /**
     *
     * An 8-byte unsigned integer unique for each record in the table that is automatically incremented each time a record is updated.
     */
    RAWVERSION(21), // int 64
    /**
     *
     * 8-byte value where the high order 4 bytes are an integer containing a Julian date, and the low order 4 bytes are internally stored as the number of milliseconds since midnight. If using the Advantage CA-Visual Objects RDDs, this is a string type. The value of this field is automatically updated with the current date and time each time a record is updated.
     */
    MODTIME(22), // datetime
    /**
     *
     * This field type allows variable length character data to be stored up to the maximum field length, which is specified when the table is created. It is similar to a character field except that the exact same data will be returned when it is read without extra blank padding on the end. If you are creating this field using the Advantage Client Engine API directly (e.g., AdsCreateTable), you must specify the type as "VarCharFox" to avoid legacy compatibility issues with an older obsolete varchar field type.
     */
    VARCHAR_FOX(23), // string
    /**
     * Variable length binary data. The maximum length of data that can be stored in the field is specified when the table is created. This is similar to the Raw field type except that the true length of the data is stored internally in the record.
     */
    VARBINARY_FOX(24), // binary
    /**
     *
     * Fixed length Unicode character field that is stored entirely in the table. The length specified for the field is the number of UTF16 code units or characters. The internal storage uses UTF16 encoding so the number of bytes occupied by the field in each record is 2 times the specified length.
     */
    NCHAR(26), // string
    /**
     * Variable length Unicode character data. The field is stored entirely in the table. The maximum length of the data that can be stored in the field is specified when the table is created. The internal storage uses UTF16 encoding so the number of bytes occupied by the field in each record is 2 times the specified length plus 2 bytes for the length.
     */
    NVARCHAR(27), // string
    /**
     * Variable length Unicode memo field. The maximum length of data that can be stored in the field is 4GB. Since UTF16 is used as the internal storage, the number of UTF16 code units is limited to 2G. The data is stored in a separate file, called a memo file.
     */
    NMEMO(28), // string


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
