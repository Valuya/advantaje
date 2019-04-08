package be.valuya.advantaje.core;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class AdvantajeIO {

    public AdvantajeIO() {
    }

    public void read(Path path) throws IOException {
        try (InputStream inputStream = Files.newInputStream(path)) {
            readBuffer(inputStream, 0x166);
            short fieldCount = readShort(inputStream);
            readBuffer(inputStream, 0x28);

            System.out.println("Field count: " + fieldCount);

            List<AdvantajeField> fields = new ArrayList<>();
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

            // after last field
            readBuffer(inputStream, 0x5);

            // data starts here
            for (AdvantajeField field : fields) {
                String fieldName = field.getName();
                System.out.print(fieldName + "|");
            }
            System.out.println();
            for (AdvantajeField field : fields) {
                String fieldName = field.getName();
                AdvantajeFieldType fieldType = field.getFieldType();
                int fieldLength = field.getLength();
                Object value = readValue(inputStream, field);
                System.out.print(fieldName + ": ");
                System.out.print("(" + fieldType + ", " + fieldLength + "): ");
                System.out.print(value + "|");
                System.out.println();
            }
            System.out.println();
        }
    }

    private Object readValue(InputStream inputStream, AdvantajeField field) throws IOException {
        AdvantajeFieldType fieldType = field.getFieldType();
        int length = field.getLength();
        switch (fieldType) {
            case LOGICAL: {
                checkLength(length, 1);
                return readBoolean(inputStream);
            }
            case NUMERIC: {
                checkLength(length, 2);
                return readShort(inputStream);
            }
            case DATE: {
                checkLength(length, 4);
                int dateInt = readInt(inputStream);
                return getDateFromInt(dateInt);
            }
            case STRING: {
                return readString(inputStream, length);
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
                return readInt(inputStream);
            }
            case SHORTINT:
                break;
            case TIME:
                break;
            case TIMESTAMP: {
                checkLength(length, 8);
                int dateInt = readInt(inputStream);
                long millisInt = readInt(inputStream);
                LocalDate localDate = getDateFromInt(dateInt);
                LocalTime localTime = LocalTime.ofNanoOfDay(millisInt * 1000L * 1000L);
                return localDate.atTime(localTime);
            }
            case AUTOINC:
                break;
            case RAW:
                break;
            case CURRENCY: {
                checkLength(length, 8);
                return readDouble(inputStream);
            }
            case MONEY:
                break;
        }
        throw new IllegalArgumentException("Unhandled field type: " + fieldType);
    }

    private void checkLength(int length, int i) {
        if (length != i) {
            throw new IllegalArgumentException();
        }
    }

    private LocalDate getDateFromInt(int dateInt) {
        if (dateInt == 0) {
            return null;
        }
        try {
            SimpleDateFormat originalFormat = new SimpleDateFormat("yyyyMMdd");
            Date date = originalFormat.parse(Integer.toString(dateInt));
            return date.toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate();
        } catch (ParseException parseException) {
            throw new IllegalArgumentException(parseException);
        }
    }

    private boolean readBoolean(InputStream inputStream) throws IOException {
        return readByte(inputStream) == 1;
    }

    private String readString(InputStream inputStream, int size) throws IOException {
        byte[] buffer = readBuffer(inputStream, size);
        return new String(buffer);
    }

    private byte readByte(InputStream inputStream) throws IOException {
        byte[] buffer = readBuffer(inputStream, 1);
        return buffer[0];
    }

    private short readShort(InputStream inputStream) throws IOException {
        byte[] bytes = readBuffer(inputStream, 2);
        ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
        return byteBuffer.order(ByteOrder.LITTLE_ENDIAN).getShort();
    }

    private int readInt(InputStream inputStream) throws IOException {
        byte[] bytes = readBuffer(inputStream, 4);
        ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
        return byteBuffer.order(ByteOrder.LITTLE_ENDIAN).getInt();
    }

    private double readDouble(InputStream inputStream) throws IOException {
        byte[] bytes = readBuffer(inputStream, 8);
        ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
        return byteBuffer.order(ByteOrder.LITTLE_ENDIAN).getDouble();
    }

    private byte[] readBuffer(InputStream inputStream, int size) throws IOException {
        byte[] buffer = new byte[size];
        int readBytes = inputStream.read(buffer);
        if (readBytes != size) {
            throw new RuntimeException("buffer underrun");
        }
        return buffer;
    }

    public static void main(String... args) throws IOException {
        AdvantajeIO advantajeIO = new AdvantajeIO();
        Path path = Paths.get("c:\\dev\\wbdata\\apizmeo-bob\\ac_ahisto.adt");
//        Path path = Paths.get("c:\\dev\\wbdata\\apizmeo-bob\\ac_chisto.adt");
//        Path path = Paths.get("c:\\dev\\wbdata\\apizmeo-bob\\ac_compan.adt");
        advantajeIO.read(path);
    }
}
