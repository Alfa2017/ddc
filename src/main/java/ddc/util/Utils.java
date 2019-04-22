package ddc.util;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.data.domain.Sort;
import org.springframework.web.multipart.MultipartFile;
import ddc.exception.ExceptionMessages;
import ddc.exception.ServerErrorDdsException;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Comparator;
import java.util.Formatter;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.springframework.data.domain.Sort.Direction.ASC;
import static org.springframework.data.domain.Sort.Direction.DESC;

@Slf4j
public class Utils {

    private Utils() {
    }

    private static final String PREFIX = "OGRN:";

    public static String removeOgrnPrefix(String ogrn) {
        if(ogrn != null && ogrn.startsWith(PREFIX)) {
            return ogrn.substring(PREFIX.length());
        }
        return ogrn;
    }

    public static String objectToJson(Object o) {
        String result = "";
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.setVisibility(objectMapper.getSerializationConfig().getDefaultVisibilityChecker()
                    .withFieldVisibility(JsonAutoDetect.Visibility.ANY)
                    .withGetterVisibility(JsonAutoDetect.Visibility.NONE)
                    .withSetterVisibility(JsonAutoDetect.Visibility.NONE)
                    .withCreatorVisibility(JsonAutoDetect.Visibility.NONE));
            result = objectMapper.writeValueAsString(o);
        } catch (JsonProcessingException e) {
            log.error(e.getMessage());
        }
        return result;
    }

    public static <T> T requestToObject(InputStream inputStream, Class<T> clazz) {
        String jsonString = "";
        try {
            jsonString = IOUtils.toString(inputStream);
        } catch (IOException e) {
            log.error("{}: {}", e.getClass(), e.getMessage());
        }
        return jsonToObject(jsonString, clazz);
    }

    public static <T> T jsonToObject(String json, Class<T> clazz) {
        T object = null;
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.setVisibility(objectMapper.getSerializationConfig().getDefaultVisibilityChecker()
                    .withFieldVisibility(JsonAutoDetect.Visibility.ANY)
                    .withGetterVisibility(JsonAutoDetect.Visibility.NONE)
                    .withSetterVisibility(JsonAutoDetect.Visibility.NONE)
                    .withCreatorVisibility(JsonAutoDetect.Visibility.NONE));
            object = objectMapper.readValue(json, clazz);
        } catch (IOException e) {
            log.error("{}: {}", e.getClass(), e.getMessage());
        }
        return object;
    }

    public static String getHash32(byte[] object) throws ServerErrorDdsException {
        MessageDigest md = null;
        try (Formatter formatter = new Formatter()) {
            md = MessageDigest.getInstance("SHA-1");
            byte[] hashBytes = md.digest(object);
            for (byte b : hashBytes) {
                formatter.format("%02x", b);
            }
            return formatter.toString().substring(0, 31);
        } catch (NoSuchAlgorithmException e) {
            log.error(e.getMessage());
            throw new ServerErrorDdsException(e.getMessage());
        }
    }

    public static boolean isBlank(String... s) {
        return s == null || s.length == 0 || s[0] == null || s[0].equals("");
    }

    public static String isNull(String s) {
        if (s == null)
            return "";
        return s;
    }

    public static Sort.Direction switchDirection(String direction) {
        return "desc".equals(direction) ? DESC : ASC;
    }

    private static String getDestinationFileName(File originalFile, Path destinationDir) throws ServerErrorDdsException {
        if (!originalFile.exists()) {
            log.error("Файл для перемещения не существует по пути {}", originalFile);
            throw new ServerErrorDdsException(ExceptionMessages.MOVE_FILE_NOT_EXIST.getMessage(originalFile));
        }

        if (!destinationDir.toFile().exists()) {
            log.error("Целевая папка не существует по пути {}", destinationDir);
            throw new ServerErrorDdsException(ExceptionMessages.TARGET_FOLDER_NOT_EXIST.getMessage(destinationDir));
        }

        String originalFileName = originalFile.getName();
        String destinationFileName = originalFileName;

        // Определяем имя файла и его расширение
        String fileName = originalFileName;
        String fileExtension = "";
        int i = originalFileName.lastIndexOf('.');
        if (i > 0) {
            fileName = originalFileName.substring(0, i);
            fileExtension = "\\." + originalFileName.substring(i + 1);
        }

        // то, что нам предстоит узнать из каталога назначения
        boolean fileAlreadyExist = false;
        Integer maxCounter = 0;

        // Паттерн копии файлов
        Pattern withCounter = Pattern.compile("^" + Pattern.quote(fileName) + "\\((\\d+)\\)" + fileExtension + "$");

        File[] files = destinationDir.toFile().listFiles();
        for (File file : files) {
            if (originalFileName.equals(file.getName())) {
                fileAlreadyExist = true;
            }

            Matcher matcher = withCounter.matcher(file.getName());
            if (matcher.matches()) {
                Integer counter = Integer.parseInt(matcher.group(1));
                if (maxCounter < counter) {
                    maxCounter = counter;
                }
            }
        }

        if (fileAlreadyExist) {
            destinationFileName = fileName + "(" + ++maxCounter + ")" + (fileExtension.length() > 1 ? fileExtension.substring(1) : "");
        }

        return destinationFileName;
    }

    public static Path safeCopy(File originalFile, Path destinationDir) throws ServerErrorDdsException {
        String destinationFileName = getDestinationFileName(originalFile, destinationDir);
        try {
            return Files.copy(originalFile.toPath(), Paths.get(destinationDir.toString(), destinationFileName));
        } catch (IOException e) {
            log.error(e.getMessage());
            throw new ServerErrorDdsException("Не удалось переместить файл: " + e.getMessage());
        }
    }

    public static void safeCopy(MultipartFile file, Path destinationDir) throws ServerErrorDdsException {
        try {
            Files.copy(file.getInputStream(), destinationDir);
        } catch (IOException e) {
            log.error(e.getMessage());
            throw new ServerErrorDdsException("Не удалось переместить файл: " + e.getMessage());
        }
    }

    /**
     *
     * @param deletionFile
     * @return - флаг, показывающий удаленность (НЕ существование) файла
     * @throws ServerErrorDdsException
     */
    public static boolean safeDelete(File deletionFile) {
        try {
            if (deletionFile.isDirectory()) {
                deleteDirectory(deletionFile.toPath());
                return true;
            }
            if (deletionFile.exists()) return deletionFile.delete();
            return true;
        } catch (ServerErrorDdsException e){
            log.error("Не удалось удалить файл {} : {}", deletionFile.getAbsolutePath(), e.getMessage());
            return false;
        }

    }
    /**
     * Безопасное копирование файла file.ext в каталог.
     * <p>
     * В случае если в каталоге назначения уже имеется файл с таким именем (file.ext), то новый файл переименовывается в
     * file(x).ext
     * <p>
     * x - ниабольшее из набора файлов с именем соответствующем маске + 1
     *
     * @param originalFile
     * @param destinationDir
     * @return
     */
    public static Path safeMove(File originalFile, Path destinationDir) throws ServerErrorDdsException {
        String destinationFileName = getDestinationFileName(originalFile, destinationDir);
        try {
            return Files.move(originalFile.toPath(), Paths.get(destinationDir.toString(), destinationFileName));
        } catch (IOException e) {
            log.error(e.getMessage());
            throw new ServerErrorDdsException(ExceptionMessages.CANT_MOVE_FILE.getMessage(e.getMessage()));
        }
    }

    //удаление директории с файлами
    public static void deleteDirectory(Path tempPath) throws ServerErrorDdsException {
        try (Stream<Path> paths = Files.walk(tempPath)) {
            paths.sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw new ServerErrorDdsException(ExceptionMessages.CANT_REMOVE_DIRECTORY.getMessage(e.getMessage()));
        }
    }

    public static <T, R> R nullSafe(T target, Function<T, R> function, Supplier<R> defaultValue) {
        try {
            return function.apply(target);
        } catch (NullPointerException npe) {
            return defaultValue.get();
        }
    }

    public static <T, R, E extends ServerErrorDdsException> R nullSafe(T target, E e, Function<T, R> function) throws E {
        try {
            return function.apply(target);
        } catch (NullPointerException npe) {
            throw e;
        }
    }

    public static <T, R> Optional<R> notNull(T target, Function<T, R> function) {
        R r;
        try {
            r = function.apply(target);
        } catch (NullPointerException ignored) {
            r = null;
        }
        return r != null ? Optional.of(r) : Optional.empty();
    }

    /**
     * Определяет путь к файлу по имени
     */
    public static Path definePathByPattern(String pattern, Path lookupFolder) throws ServerErrorDdsException {
        try (Stream<Path> paths = Files.walk(lookupFolder)) {
            return paths.filter(p -> p.getFileName().toFile().getName().matches(pattern))
                    .findFirst()
                    .orElseThrow(() -> new FileNotFoundException(ExceptionMessages.CANT_FIND_FILE_BY_PATTERN.getMessage(pattern, lookupFolder)));
        } catch (IOException e) {
            throw new ServerErrorDdsException(e.getMessage());
        }
    }
}
