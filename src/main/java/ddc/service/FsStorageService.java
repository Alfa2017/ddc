package ddc.service;


import ddc.service.storage.exceptions.StorageServiceException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ddc.exception.ServerErrorDdsException;
import ddc.util.Utils;
import ddc.util.ZipUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.UUID;

/**
 * Класс для работы с документами в файловой системе
 */
@Slf4j
@Service
public class FsStorageService {

    private String storageFolder;

    public static String buildUri(String id, String directory) {
        File directoryFile = new File(directory);
        Collection<File> filesContainsId = FileUtils.listFiles(directoryFile, new WildcardFileFilter("*" + id + "*"), null);
        if (filesContainsId.isEmpty()) return "";
        return filesContainsId.stream().findFirst().get().getAbsolutePath();
    }

    public FsStorageService(@Value("${storage.localpath}") String storageFolder) {
        File folder = new File(storageFolder);
        if (!folder.exists()) {
            if (folder.mkdirs()) {
                log.debug("Создан каталог: {}", storageFolder);
            } else {
                log.debug("Не удалось создать каталог: {}", storageFolder);
            }
        }

        this.storageFolder = storageFolder;
    }

    public String save(byte[] content) throws StorageServiceException {
        return save(content, UUID.randomUUID().toString());
    }


    /**
     * Сохраняет документ по пути {@code storageFolder}
     *
     * @param content Массив байт, представляющий собой документ
     * @param fileName
     * @return Id файла в хранилище
     */
    public String save(byte[] content, String fileName) throws StorageServiceException {
        Path path = Paths.get(storageFolder, fileName);

        try (FileOutputStream fos = new FileOutputStream(path.toFile());
             BufferedOutputStream bos = new BufferedOutputStream(fos)) {
            bos.write(content, 0, content.length);
            bos.flush();
        } catch (IOException e) {
            log.error(e.getMessage());
            throw new StorageServiceException(e.getMessage());
        }
        return fileName;
    }

    public String save(File file) throws ServerErrorDdsException {
        String fileName = Utils.getHash32(UUID.randomUUID().toString().getBytes()) + "." + FilenameUtils.getExtension(file.getName());
        Path path = Paths.get(storageFolder, fileName);
        try {
            Files.copy(file.toPath(), path);
        } catch (IOException e) {
            log.error(e.getMessage());
            throw new StorageServiceException(e.getMessage());
        }
        return fileName;
    }

    /**
     * Возвращает массив байт, представляющий собой документ, по его абсолютному пути
     *
     * @param uri Id Файла в хранилище
     * @return Массив байт, представляющий собой документ
     * @throws IOException
     */
    public byte[] get(String uri) throws StorageServiceException {
        if (uri == null || uri.isEmpty()) {
            throw new IllegalArgumentException("Uri can't be null or empty");
        }
        byte[] input = null;
        try (FileInputStream fis = new FileInputStream(storageFolder + File.separator + uri);
             BufferedInputStream bis = new BufferedInputStream(fis)) {
            // Будем считать что файлы не будут более Integer.MAX_VALUE
            input = new byte[fis.available()];
            bis.read(input);
        } catch (IOException e) {
            log.error(e.getMessage());
            throw new StorageServiceException(e.getMessage());
        }
        return input;
    }


    public File createTempFileFromByteArr(byte[] buf, String prefix, String suffix) throws ServerErrorDdsException {
        try {
            File tempFile = File.createTempFile("dds_" + prefix, suffix);
            Files.write(tempFile.toPath(), buf);
            return tempFile;
        } catch (IOException e) {
            throw new ServerErrorDdsException(e.getMessage());
        }
    }

    public Path getUnzippedPath(Path tempDir, byte[] rawDocument) throws ServerErrorDdsException {
        try {
            String randomId = UUID.randomUUID().toString();
            //записываем архив в файл
            Path zip = tempDir.resolve(randomId + ".zip");
            Files.write(zip, rawDocument);

            Path dir = tempDir.resolve(randomId);
            //разархивируем архив
            ZipUtils.unzipRecursively(zip.toFile(), dir.toFile());
            return dir;
        } catch (IOException e) {
            throw new ServerErrorDdsException(e.getMessage());
        }
    }

}
