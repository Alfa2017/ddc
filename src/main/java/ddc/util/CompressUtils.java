package ddc.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

@Slf4j
public class CompressUtils {

    private CompressUtils() {
        throw new IllegalStateException("Utility class");
    }

    public static String objectToJson(Object o) {
        String result = "";
        try {
            result = new ObjectMapper().writeValueAsString(o);
        } catch (JsonProcessingException e) {
            log.error(e.getMessage());
        }
        return result;
    }

    public static <T> T jsonToObject(String json, Class<T> clazz) {
        if(Utils.isBlank(json)) return null;
        T object = null;
        try {
            object = new ObjectMapper().readValue(json, clazz);
        } catch (IOException e) {
            log.error(e.getMessage());
        }
        return object;
    }

    public static boolean isGzip(byte[] bytes) {
        if (bytes == null || bytes.length < 2) {
            return false;
        }
        int head = ((int) bytes[0] & 0xff) | ((bytes[1] << 8) & 0xff00);
        return (GZIPInputStream.GZIP_MAGIC == head);
    }

    public static byte[] compress(byte[] data) {
        byte[] compressed = null;
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream(data.length)) {
            GZIPOutputStream gzip = new GZIPOutputStream(bos);
            gzip.write(data);
            gzip.close();
            compressed = bos.toByteArray();
        } catch (IOException e) {
            log.error(e.getMessage());
        }
        return compressed;
    }

    public static String decompress(byte[] compressed) {
        if(compressed.length == 0) return "";
        StringBuilder sb = new StringBuilder();
        try (ByteArrayInputStream bis = new ByteArrayInputStream(compressed)) {
            GZIPInputStream gis = new GZIPInputStream(bis);
            BufferedReader br = new BufferedReader(new InputStreamReader(gis));
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            br.close();
            gis.close();
        } catch (IOException e) {
            log.error(e.getMessage());
        }
        return sb.toString();
    }

    public static byte[] compressObject(Object o) {
        return compress(objectToJson(o).getBytes());
    }

    public static <T> T decompressToObject(byte[] compressed, Class<T> clazz) {
        String decompressed = decompress(compressed);
        return jsonToObject(decompressed, clazz);
    }
}
