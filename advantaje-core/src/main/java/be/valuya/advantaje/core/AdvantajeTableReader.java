package be.valuya.advantaje.core;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.JulianFields;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AdvantajeTableReader {
    private long offset;

    public AdvantajeTableMetaData openTable(InputStream inputStream, Charset charset) {
        offset = 0;
        List<AdvantajeField<?>> fields = new ArrayList<>();
        readBuffer(inputStream, 0x18); // Advantage table headers
        int recordCount = readInt(inputStream);
        byte[] unknownB = readBuffer(inputStream, 0x14a);
        short fieldCount = readShort(inputStream);
        byte[] unknownC = readBuffer(inputStream, 0x28);

        for (int i = 0; i < fieldCount; i++) {
            String fieldName = readString(inputStream, 0x80, charset);

            byte unknown1 = readByte(inputStream);
            int fieldTypeCode = readShort(inputStream);
            AdvantajeFieldType fieldType = AdvantajeFieldType.fromCode(fieldTypeCode);
            int fieldStartOffset = readShort(inputStream);
            int unknown2 = readShort(inputStream);
            int fieldLength = readShort(inputStream);
            byte[] unknown3 = readBuffer(inputStream, 0x3f);

            AdvantajeField field = new AdvantajeField(fieldName, fieldType);
            field.setLength(fieldLength);
            fields.add(field);
        }

        return new AdvantajeTableMetaData(recordCount, fields);
    }


    public AdvantajeRecord readNextLine(InputStream inputStream, List<AdvantajeField<?>> fields, Charset charset) {
        // after last field
        readByte(inputStream); // unknown
        readInt(inputStream); // unknown
        AdvantajeRecord advantajeRecord = new AdvantajeRecord();
        fields.stream()
                .map(field -> getAdvantajeValue(inputStream, field, charset))
                .forEach(advantajeRecord::put);
        return advantajeRecord;
    }

    private <T> AdvantajeValue<T> getAdvantajeValue(InputStream inputStream, AdvantajeField<T> field, Charset charset) {
        Optional<T> valueOptional = readValue(inputStream, field, charset);
        return new AdvantajeValue<>(field, valueOptional);
    }

    private <T> Optional<T> readValue(InputStream inputStream, AdvantajeField<T> field, Charset charset) {
        AdvantajeFieldType fieldType = field.getFieldType();
        int length = field.getLength();
//        System.out.println(fieldType + "(" + length + ") " + field.getName() + "@" + offset);
        switch (fieldType) {
            case LOGICAL: {
                checkLength(length, 1);
                return (Optional<T>) readBooleanOptional(inputStream);
            }
            case NUMERIC: {
                checkLength(length, 2);
                short shortValue = readShort(inputStream);
                return (Optional<T>) Optional.of(shortValue);
            }
            case DATE: {
                checkLength(length, 4);
                return (Optional<T>) readIntegerOptional(inputStream)
                        .flatMap(this::getDateFromIntOptional);
            }
            case STRING: {
                String stringValue = readString(inputStream, length, charset);
                return (Optional<T>) Optional.of(stringValue);
            }
            case MEMO:
                break;
            case BINARY: {
                return (Optional<T>) readBufferOptional(inputStream, length);
            }
            case IMAGE: {
                return (Optional<T>) readBufferOptional(inputStream, length);
            }
            case DOUBLE: {
                checkLength(length, 8);
                return (Optional<T>) readDoubleOptional(inputStream);
            }
            case INTEGER: {
                checkLength(length, 4);
                return (Optional<T>) readIntegerOptional(inputStream);
            }
            case SHORTINT: {
                checkLength(length, 2);
                return (Optional<T>) Optional.of(readShort(inputStream));
            }
            case TIME: {
                checkLength(length, 4);
                return (Optional<T>) readLocalTimeOptional(inputStream);
            }
            case TIMESTAMP: {
                checkLength(length, 8);
                return (Optional<T>) readLocalDateTimeOptional(inputStream);
            }
            case AUTOINC:
                checkLength(length, 4);
                return (Optional<T>) readIntegerOptional(inputStream);
            case RAW: {
                return (Optional<T>) readBufferOptional(inputStream, length);
            }
            case CURRENCY: {
                checkLength(length, 8);
                return (Optional<T>) readDoubleOptional(inputStream);
            }
            case MONEY: {
                checkLength(length, 8);
                return (Optional<T>) readLongOptional(inputStream);
            }
            case CISSTRING: {
                String stringValue = readString(inputStream, length, charset);
                return (Optional<T>) Optional.of(stringValue);
            }
            case RAWVERSION: {
                checkLength(length, 4);
                return (Optional<T>) readIntegerOptional(inputStream);
            }
            case MODTIME: {
                checkLength(length, 8);
                return (Optional<T>) readLocalTimeOptional(inputStream);
            }
            case VARCHAR_FOX: {
                String stringValue = readVarString(inputStream, length, charset);
                return (Optional<T>) Optional.of(stringValue);
            }
            case VARBINARY_FOX: {
                return (Optional<T>) readBufferOptional(inputStream, length);
            }
            case NCHAR: {
                String stringValue = readString(inputStream, length * 2, StandardCharsets.UTF_16LE);
                return (Optional<T>) Optional.of(stringValue);
            }
            case NVARCHAR: {
                String stringValue = readVarString(inputStream, length * 2 + 2, StandardCharsets.UTF_16LE);
                return (Optional<T>) Optional.of(stringValue);
            }
            case NMEMO:
                break;
        }
        throw new IllegalArgumentException("Unhandled field type: " + fieldType);
    }

    private Optional<LocalDateTime> readLocalDateTimeOptional(InputStream inputStream) {
        Optional<LocalDate> localDateOptional = readIntegerOptional(inputStream)
                .flatMap(this::getDateFromIntOptional);
        Optional<LocalTime> localTimeOptional = readLocalTimeOptional(inputStream);
        return localDateOptional
                .flatMap(localDate -> localTimeOptional.map(localTime -> addLocalTime(localDate, localTime)));
    }

    private Optional<LocalTime> readLocalTimeOptional(InputStream inputStream) {
        return readIntegerOptional(inputStream)
                .map(this::convertMillisToLocalTime);
    }

    private LocalDateTime addLocalTime(LocalDate localDate, LocalTime localTime) {
        return localDate.atTime(localTime);
    }

    private LocalTime convertMillisToLocalTime(long millis) {
        if (millis < 0) {
            return LocalTime.MIN;
        }
        return LocalTime.ofNanoOfDay(millis * 1000L * 1000L);
    }

    private void checkLength(int length, int i) {
        if (length != i) {
            throw new IllegalArgumentException();
        }
    }

    private Optional<LocalDate> getDateFromIntOptional(int dateInt) {
        if (dateInt == 0) {
            return Optional.empty();
        }
        LocalDate localDate = LocalDate.MIN.with(JulianFields.JULIAN_DAY, dateInt);
        return Optional.of(localDate);
    }

    private boolean readBoolean(InputStream inputStream) {
        return readBooleanOptional(inputStream)
                .orElseThrow(() -> new IllegalArgumentException("Missing expected boolean value"));
    }

    private Optional<Boolean> readBooleanOptional(InputStream inputStream) {
        byte byteValue = readByte(inputStream);
        if (byteValue == 'T') {
            return Optional.of(true);
        }
        if (byteValue == 'F') {
            return Optional.of(false);
        }
        return Optional.empty();
    }

    private String readString(InputStream inputStream, int size, Charset charset) {
        byte[] buffer = readBuffer(inputStream, size);
        String untrimmedString = new String(buffer, charset);
        return untrimmedString.replace("\0", "").trim();
    }

    /**
     * Read a string field which store the string size at the end of the field.
     */
    private String readVarString(InputStream inputStream, int fieldSize, Charset charset) {
        byte[] buffer = readBuffer(inputStream, fieldSize - 2);
        String untrimmedString = new String(buffer, charset);
        short size = readShort(inputStream);
        return untrimmedString.substring(0, size).trim();
    }

    public byte readByte(InputStream inputStream) {
        byte[] bytes = readBuffer(inputStream, 1);
        return bytes[0];
    }

    private short readShort(InputStream inputStream) {
        byte[] bytes = readBuffer(inputStream, 2);
        ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
        return byteBuffer.order(ByteOrder.LITTLE_ENDIAN).getShort();
    }

    private int readInt(InputStream inputStream) {
        return readIntegerOptional(inputStream)
                .orElseThrow(() -> new IllegalArgumentException("Missing expected int value"));
    }

    private Optional<Integer> readIntegerOptional(InputStream inputStream) {
        byte[] bytes = readBuffer(inputStream, 4);
        ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
        int intValue = byteBuffer.order(ByteOrder.LITTLE_ENDIAN).getInt();
        if (intValue == Integer.MIN_VALUE || intValue == Integer.MAX_VALUE) {
            return Optional.empty();
        }
        return Optional.of(intValue);
    }

    private Optional<Long> readLongOptional(InputStream inputStream) {
        byte[] bytes = readBuffer(inputStream, 8);
        ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
        long longValue = byteBuffer.order(ByteOrder.LITTLE_ENDIAN).getLong();
//        if (intValue == Integer.MIN_VALUE || intValue == Integer.MAX_VALUE) {
//            return Optional.empty();
//        }
        return Optional.of(longValue);
    }

    private double readDouble(InputStream inputStream) {
        return readDoubleOptional(inputStream)
                .orElseThrow(() -> new IllegalArgumentException("Missing expected double value"));
    }

    private Optional<Double> readDoubleOptional(InputStream inputStream) {
        byte[] bytes = readBuffer(inputStream, 8);
        ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
        double doubleValue = byteBuffer.order(ByteOrder.LITTLE_ENDIAN).getDouble();
        if (doubleValue == -1.58E-322 || doubleValue == Double.MIN_VALUE || doubleValue == Double.MAX_VALUE) {
            return Optional.empty();
        }
        return Optional.of(doubleValue);
    }

    private byte[] readBuffer(InputStream inputStream, int size) {
        return readBufferOptional(inputStream, size)
                .orElseThrow(() -> new RuntimeException("buffer underrun"));
    }

    private Optional<byte[]> readBufferOptional(InputStream inputStream, int size) {
        try {
            byte[] buffer = new byte[size];
            int readBytes = inputStream.read(buffer);
            offset += readBytes;
            if (readBytes != size) {
                return Optional.empty();
            }
            return Optional.of(buffer);
        } catch (IOException exception) {
            throw new AdvantajeException("Error reading buffer", exception);
        }
    }

}
