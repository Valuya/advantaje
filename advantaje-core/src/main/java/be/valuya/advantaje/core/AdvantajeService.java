package be.valuya.advantaje.core;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.JulianFields;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class AdvantajeService {

    public static final Charset DEFAULT_CHARSET = Charset.forName("iso-8859-1");


    /**
     * @param inputStream The table inputstream will be closed
     * @return
     */
    public Stream<AdvantajeRecord> streamTable(InputStream inputStream) {
        return streamTable(inputStream, DEFAULT_CHARSET);
    }

    /**
     * @param inputStream The table inputstream will be closed
     * @param charset
     * @return
     */
    public Stream<AdvantajeRecord> streamTable(InputStream inputStream, Charset charset) {
        AdvantajeSpliteraor spliteraor = new AdvantajeSpliteraor(inputStream, charset);
        return StreamSupport.stream(spliteraor, false)
                .onClose(() -> spliteraor.close());
    }


    private static void printRecord(AdvantajeRecord advantajeRecord) {
        advantajeRecord.getValueMap()
                .values()
                .forEach(AdvantajeService::printFieldValue);
        System.out.println("---------------------------------------------------------------------------");
    }

    private static <T> void printFieldValue(AdvantajeValue<T> advantajeValue) {
        AdvantajeField<? extends T> field = advantajeValue.getField();
        Optional<T> valueOptional = advantajeValue.getValueOptional();
        String fieldName = field.getName();
        AdvantajeFieldType fieldType = field.getFieldType();
        int fieldLength = field.getLength();

        System.out.print(fieldName + ": ");
        System.out.print("(" + fieldType + ", " + fieldLength + "): ");
        String valueStr = valueOptional.map(Object::toString).orElse("[-]");
        System.out.print(valueStr);
//        System.out.print("|");
        System.out.println();
    }

    public static void main(String... args) throws IOException {
//        Path path = Paths.get("c:\\dev\\wbdata\\apizmeo-bob\\ac_ahisto.adt");
//        Path path = Paths.get("c:\\dev\\wbdata\\apizmeo-bob\\ac_chisto.adt");
//        Path path = Paths.get("/home/cghislai/dev/valuya/gestemps/res/bob-data/APIZMEOData/ac_compan.adt");
//        Path path = Paths.get("/home/cghislai/dev/valuya/gestemps/res/bob-data/APIZMEOData/ac_linkdoc.adt");
//        Path path = Paths.get("c:\\dev\\wbdata\\apizmeo-bob\\ac_period.adt");

        Path fodlerPath = Paths.get("/home/cghislai/dev/valuya/gestemps/res/bob-data/DL");
//        Path fodlerPath = Paths.get("/home/cghislai/dev/valuya/gestemps/res/bob-data/DL");
        Files.list(fodlerPath)
                .sorted(Comparator.comparing(p -> p.getFileName().toString()))
                .filter(p -> p.getFileName().toString().endsWith(".adt"))
                .peek(path -> System.out.print(" Table " + path.getFileName().toString() + " : "))
                .map(AdvantajeService::getTableMetadata)
                .forEach(p -> System.out.println(p.getRecordCount() + " records"));

//        Path path = Paths.get("/home/cghislai/dev/valuya/gestemps/res/bob-data/APIZMEOData/fi_2017nadt.adt");
//        Path path = Paths.get("/home/cghislai/dev/valuya/gestemps/res/bob-data/DL/ac_entryl.adt");
//        Path path = Paths.get("/home/cghislai/dev/valuya/gestemps/res/bob-data/APIZMEOData/ac_ahisto.adt");
//        Path path = Paths.get("/home/cghislai/dev/valuya/gestemps/res/bob-data/APIZMEOData/ac_accoun.adt");
//        Path path2 = Paths.get("/home/cghislai/dev/valuya/gestemps/res/bob-data/DL/dm_invdoc.adt");
//        debugTable(path);
        Path path2 = Paths.get("/home/cghislai/dev/valuya/gestemps/res/bob-data/DL/ac_accoun.adt");
        debugTable(path2);

    }

    private static AdvantajeTableMetaData getTableMetadata(Path path) {
        try {
            InputStream inputStream = Files.newInputStream(path);
            AdvantajeTableReader tableReader = new AdvantajeTableReader();
            AdvantajeTableMetaData advantajeTableMetaData = tableReader.openTable(inputStream, DEFAULT_CHARSET);
            return advantajeTableMetaData;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void debugTable(Path path) {
        AdvantajeService advantajeService = new AdvantajeService();
        try {
            AdvantajeTableMetaData tableMetadata = getTableMetadata(path);
            System.out.println("===================");
            tableMetadata.getFields()
                    .forEach(f -> System.out.println(f.getFieldType() + " - " + f.getLength() + " - " + f.getName()));
            System.out.println("records: " + tableMetadata.getRecordCount());
            System.out.println("===================");

            InputStream inputStream = Files.newInputStream(path);
            advantajeService.streamTable(inputStream, DEFAULT_CHARSET)
                    .forEach(AdvantajeService::printRecord);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
