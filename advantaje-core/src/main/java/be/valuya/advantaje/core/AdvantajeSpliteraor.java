package be.valuya.advantaje.core;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Spliterator;
import java.util.function.Consumer;

public class AdvantajeSpliteraor implements Spliterator<AdvantajeRecord>, Closeable {


    private final AdvantajeTableReader tableReader;
    private final AdvantajeTableMetaData table;
    private final List<AdvantajeField<?>> fields;
    private final int recordCount;
    private int curIndex;
    private InputStream inputStream;
    private Charset charset;

    public AdvantajeSpliteraor(InputStream inputStream, Charset charset) {
        this.inputStream = inputStream;
        this.charset = charset;
        this.tableReader = new AdvantajeTableReader();
        table = tableReader.openTable(inputStream, charset);

        fields = table.getFields();
        recordCount = table.getRecordCount();
        curIndex = 0;
    }

    @Override
    public boolean tryAdvance(Consumer<? super AdvantajeRecord> action) {
        if (curIndex >= recordCount) {
            return false;
        }
        try {
            AdvantajeRecord advantajeRecord = tableReader.readNextLine(inputStream, fields, charset);
            action.accept(advantajeRecord);
            curIndex++;
            return true;
        } catch (Exception e) {
            throw new AdvantajeException(e);
        }
    }

    @Override
    public Spliterator<AdvantajeRecord> trySplit() {
        return null;
    }

    @Override
    public long estimateSize() {
        return recordCount;
    }

    @Override
    public long getExactSizeIfKnown() {
        return recordCount;
    }

    @Override
    public int characteristics() {
        return Spliterator.ORDERED | Spliterator.NONNULL | Spliterator.IMMUTABLE;
    }

    @Override
    public void close() {
        if (inputStream != null) {
            try {
                inputStream.close();
            } catch (IOException e) {
                throw new AdvantajeException(e);
            }
        }
    }
}
