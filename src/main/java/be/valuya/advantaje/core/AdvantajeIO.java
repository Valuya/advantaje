package be.valuya.advantaje.core;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.JulianFields;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

public class AdvantajeIO {

    public AdvantajeIO() {
    }

    public void read(Path path) {
        try (InputStream inputStream = Files.newInputStream(path)) {
            readBuffer(inputStream, 0x18);
            int recordCount = readInt(inputStream);
            readBuffer(inputStream, 0x14a);
            short fieldCount = readShort(inputStream);
            readBuffer(inputStream, 0x28);

            System.out.println("Field count: " + fieldCount);

            List<AdvantajeField<?>> fields = new ArrayList<>();
            for (int i = 0; i < fieldCount; i++) {
                String fieldName = readString(inputStream, 0x80);

                byte unknown1 = readByte(inputStream);
                int fieldTypeCode = readShort(inputStream);
                AdvantajeFieldType fieldType = AdvantajeFieldType.fromCode(fieldTypeCode);
                int fieldStartOffset = readShort(inputStream);
                int unknown2 = readShort(inputStream);
                int fieldLength = readShort(inputStream);
                readBuffer(inputStream, 0x3f);

                AdvantajeField field = new AdvantajeField(fieldName, fieldType);
                field.setLength(fieldLength);
                fields.add(field);
            }

            for (AdvantajeField field : fields) {
                String fieldName = field.getName();
                AdvantajeFieldType fieldType = field.getFieldType();
                int fieldLength = field.getLength();
                String message = MessageFormat.format("{0}: {1} - {2}", fieldName, fieldType, fieldLength);
                System.out.println(message);
            }

            // data starts here
            for (AdvantajeField field : fields) {
                String fieldName = field.getName();
                System.out.print(fieldName + "|");
            }
            System.out.println();
            System.out.println("---------------------------------------------------------------------------");
            for (int i = 0; i < recordCount; i++) {
                System.out.println("-----" + i);
                Map<AdvantajeField<?>, ? extends Optional<?>> lineMap = readLineMap(inputStream, fields);
                for (AdvantajeField<?> field : fields) {
                    Optional<?> valueOptional = lineMap.get(field);
                    printFieldValue(field, valueOptional);
                }
                System.out.println("---------------------------------------------------------------------------");

            }
        } catch (IOException ioException) {
            throw new AdvantageException("Cannot read file.", ioException);
        }
    }

    private Map<AdvantajeField<?>, ? extends Optional<?>> readLineMap(InputStream inputStream, List<AdvantajeField<?>> fields) {
        // after last field
        readByte(inputStream);
        readInt(inputStream);
        return fields.stream()
                .collect(Collectors.toMap(Function.identity(), field -> readValue(inputStream, field)));
    }

    private void printFieldValue(AdvantajeField<?> field, Optional<?> valueOptional) {
        String fieldName = field.getName();
        AdvantajeFieldType fieldType = field.getFieldType();
        int fieldLength = field.getLength();

        System.out.print(fieldName + ": ");
        System.out.print("(" + fieldType + ", " + fieldLength + "): ");
        String valueStr = valueOptional.map(Object::toString).orElse("-");
        System.out.print(valueStr + "|");
        System.out.println();
    }

    private <T> Optional<T> readValue(InputStream inputStream, AdvantajeField<T> field) {
        AdvantajeFieldType fieldType = field.getFieldType();
        int length = field.getLength();
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
                String stringValue = readString(inputStream, length);
                return (Optional<T>) Optional.of(stringValue);
            }
            case MEMO:
                break;
            case BINARY:
                break;
            case IMAGE:
                break;
            case DOUBLE:
                break;
            case INTEGER: {
                checkLength(length, 4);
                return (Optional<T>) readIntegerOptional(inputStream);
            }
            case SHORTINT:
                break;
            case TIME:
                break;
            case TIMESTAMP: {
                checkLength(length, 8);
                return (Optional<T>) readLocalDateTimeOptional(inputStream);
            }
            case AUTOINC:
                break;
            case RAW:
                break;
            case CURRENCY: {
                checkLength(length, 8);
                return (Optional<T>) readDoubleOptional(inputStream);
            }
            case MONEY:
                break;
        }
        throw new IllegalArgumentException("Unhandled field type: " + fieldType);
    }

    private Optional<LocalDateTime> readLocalDateTimeOptional(InputStream inputStream) {
        return readIntegerOptional(inputStream)
                .flatMap(this::getDateFromIntOptional)
                .flatMap(localDate -> readTimePartOptional(inputStream, localDate));
    }

    private Optional<LocalDateTime> readTimePartOptional(InputStream inputStream, LocalDate localDate) {
        Optional<Integer> millisOptional = readIntegerOptional(inputStream);
        return millisOptional.map(millisInt -> addMillis(localDate, millisInt));
    }

    private LocalDateTime addMillis(LocalDate localDate, Integer millisInt) {
        LocalTime localTime = LocalTime.ofNanoOfDay(millisInt * 1000L * 1000L);
        return localDate.atTime(localTime);
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
        boolean byteValue = readByte(inputStream) != 'F';
        return Optional.of(byteValue);
    }

    private String readString(InputStream inputStream, int size) {
        byte[] buffer = readBuffer(inputStream, size);
        return new String(buffer);
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
        if (intValue == Integer.MIN_VALUE) {
            return Optional.empty();
        }
        return Optional.of(intValue);
    }

    private double readDouble(InputStream inputStream) {
        return readDoubleOptional(inputStream)
                .orElseThrow(() -> new IllegalArgumentException("Missing expected double value"));
    }

    private Optional<Double> readDoubleOptional(InputStream inputStream) {
        byte[] bytes = readBuffer(inputStream, 8);
        ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
        double doubleValue = byteBuffer.order(ByteOrder.LITTLE_ENDIAN).getDouble();
        if (doubleValue == -1.58E-322 || doubleValue == Double.MIN_VALUE) {
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
            if (readBytes != size) {
                return Optional.empty();
            }
            return Optional.of(buffer);
        } catch (IOException exception) {
            throw new AdvantageException("Error reading buffer", exception);
        }
    }

    public static void main(String... args) {
        AdvantajeIO advantajeIO = new AdvantajeIO();
        Path path = Paths.get("c:\\dev\\wbdata\\apizmeo-bob\\ac_ahisto.adt");
//        Path path = Paths.get("c:\\dev\\wbdata\\apizmeo-bob\\ac_chisto.adt");
//        Path path = Paths.get("c:\\dev\\wbdata\\apizmeo-bob\\ac_compan.adt");
        advantajeIO.read(path);
    }
}
