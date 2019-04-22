package ddc.util;

import ddc.util.struct.CsvPatchStruct;
import ddc.util.struct.CsvStruct;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.nio.file.Files;
import java.util.List;
import java.util.stream.Stream;

@Slf4j
public class CsvUtils {

    public static void writeCsv(File csv, CsvStruct csvStruct){
        try (PrintWriter writer = new PrintWriter(csv)){
            StringBuilder sb = new StringBuilder();
            for (List<String> itemsInLine: csvStruct.getLines()){
                if (itemsInLine == null) continue;
                for (int i = 0; i < itemsInLine.size() - 1; i++ ){
                    sb.append(itemsInLine.get(i));
                    sb.append(',');
                }
                sb.append(itemsInLine.get(itemsInLine.size() - 1));
                sb.append('\n');
            }
            writer.write(sb.toString());
        } catch (FileNotFoundException e){
            log.error("Ошабка при попытки записи в csv файл {}" + e.getMessage());
        }
    }

    public static CsvStruct parseCsv(File csv){
        try (Stream<String> lines = Files.lines(csv.toPath())){
            CsvStruct struct = new CsvStruct();
            lines.forEach(x -> struct.setLines(x.split(",")));
            switch (struct.getType()){
                case MORTGAGES_PATCH:{
                    return CsvPatchStruct.fromCsvStruct(struct);
                }
                default: {
                    return struct;
                }
            }
        } catch (IOException e){
            log.error("Ошабка при попытки чтения csv файла {}" + e.getMessage());
            return null;
        }
    }

    public static CsvStruct getCsvFromByteArray(byte[] bytes){
        try {
            File f = Files.createTempFile("mortgagesPatch", ".csv").toFile();
            FileUtils.writeByteArrayToFile(f, bytes);
            return parseCsv(f);
        } catch (IOException e){
            log.error("Ошабка при попытки преобразования массива байт в csv файл {}" + e.getMessage());
            return null;
        }
    }
}
