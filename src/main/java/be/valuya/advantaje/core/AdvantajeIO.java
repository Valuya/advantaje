package be.valuya.advantaje.core;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;

public class AdvantajeIO {

    public AdvantajeIO() {
    }

    public void read(Path path) throws IOException {
        try (InputStream inputStream = Files.newInputStream(path)) {
            readBuffer(inputStream, 0x165);
            int fieldCount = readWord(inputStream);
            readBuffer(inputStream, 0x29);

            System.out.println("Field count: " + fieldCount);

            int lastFieldStartOffset = 5;
            for (int i = 0; i < fieldCount ; i++) {
                String fieldName = readString(inputStream, 0x80);

                int fieldTypeInt = readWord(inputStream);
                int fieldStartOffset = readWord(inputStream);
                int fieldSize = fieldStartOffset - lastFieldStartOffset;
                int unknown = readInt(inputStream);
            readBuffer(inputStream, 0x40);

                String message = MessageFormat.format("{0}: {1} - {2}", fieldName, fieldTypeInt, fieldSize);
                System.out.println(message);

                lastFieldStartOffset = fieldStartOffset;
            }

        }
    }

    private String readString(InputStream inputStream, int size) throws IOException {
        byte[] buffer = readBuffer(inputStream, size);
        return new String(buffer);
    }

    private byte readByte(InputStream inputStream) throws IOException {
        byte[] buffer = readBuffer(inputStream, 1);
        return buffer[0];
    }

    private int readWord(InputStream inputStream) throws IOException {
        byte[] buffer = readBuffer(inputStream, 2);
        return buffer[0] << 8 | buffer[1];
    }
    private int readInt(InputStream inputStream) throws IOException {
        byte[] buffer = readBuffer(inputStream, 4);
        ByteBuffer byteBuffer = ByteBuffer.wrap(buffer, 0, 4);
        return byteBuffer.getInt();
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
