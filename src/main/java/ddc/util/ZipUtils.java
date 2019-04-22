package ddc.util;

import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

@Slf4j
public class ZipUtils {

    private static int BUFFER_SIZE = 4096;

    public static String unZipXmlStruct(Path pathToZip) throws IOException {
        String zipRequestFile = null;
        Path tempDirectory = Files.createTempDirectory(UUID.randomUUID().toString());
        String fileZip = pathToZip.toAbsolutePath().toString();
        byte[] buffer = new byte[1024];
        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(fileZip))) {
            ZipEntry zipEntry = zis.getNextEntry();
            while (zipEntry != null) {
                String fileName = zipEntry.getName().toLowerCase();
                if (zipEntry.isDirectory()) {
                    File newFile = new File(tempDirectory.toAbsolutePath().toString() + File.separator + fileName);
                    newFile.mkdirs();
                    zipEntry = zis.getNextEntry();
                } else {
                    File newFile = new File(tempDirectory.toAbsolutePath().toString() + File.separator + fileName);
                    if (fileName.contains("request") && fileName.endsWith(".xml")) {
                        zipRequestFile = newFile.getAbsolutePath();
                    }
                    new File(newFile.getParent()).mkdirs();
                    try (FileOutputStream fos = new FileOutputStream(newFile)) {
                        int len;
                        while ((len = zis.read(buffer)) > 0) {
                            fos.write(buffer, 0, len);
                        }
                    }
                    zis.closeEntry();
                    zipEntry = zis.getNextEntry();
                }
            }
            zis.closeEntry();
        } catch (IOException e) {
            log.error(e.getMessage());
        }
        return zipRequestFile;
    }

    public static File zip(List<File> srcFiles, File destination) {
        try (FileOutputStream fos = new FileOutputStream(destination);
             ZipOutputStream zipOut = new ZipOutputStream(fos)) {
            for (File srcFile : srcFiles) {
                ZipEntry zipEntry = new ZipEntry(srcFile.getName());
                zipOut.putNextEntry(zipEntry);

                byte[] bytes = new byte[BUFFER_SIZE];
                int length;
                try (FileInputStream fis = new FileInputStream(srcFile)) {
                    while ((length = fis.read(bytes)) >= 0) {
                        zipOut.write(bytes, 0, length);
                    }
                }
            }
        } catch (IOException e) {
            log.error(e.getMessage());
        }

        return destination;
    }

    public static void unzip(File source, File destination) throws IOException {
        if (!destination.exists()) {
            Files.createDirectories(destination.toPath());
        }

        try (FileInputStream fis = new FileInputStream(source); ZipInputStream zis = new ZipInputStream(fis, StandardCharsets.ISO_8859_1)) {

            ZipEntry zipEntry = zis.getNextEntry();

            byte[] buffer = new byte[BUFFER_SIZE];
            while (zipEntry != null) {
                String fileName = zipEntry.getName();
                if (zipEntry.isDirectory()) {
                    Files.createDirectories(destination.toPath().resolve(fileName));
                } else {
                    File newFile = new File(destination.toString() + File.separator + fileName);
                    try (FileOutputStream fos = new FileOutputStream(newFile)) {
                        int len;
                        while ((len = zis.read(buffer)) > 0) {
                            fos.write(buffer, 0, len);
                        }
                    }
                }
                zipEntry = zis.getNextEntry();
            }
            zis.closeEntry();
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    /**
     * Разархивирует zip файлы, затем рекурсивно проходит по файлам внутри каталога, куда было разархивирование.
     * Если один из файлов оказывается zip  архивом, для него создается директория, путь к которой
     * передается в качестве параметра при рекурсивном вызове этого же метода
     *
     * @param archive     Файл архива, представленный в формате zip
     * @param destination Путь, куда будет разархивирование
     * @return true при успешном рекурсивном разархивировании архива и всех вложенных в него архивов и
     * false в  случае исключения при разархивировании
     * @throws IOException
     */
    public static void unzipRecursively(File archive, File destination) throws IOException {
        ZipUtils.unzip(archive, destination);
        for (File file : Objects.requireNonNull(destination.listFiles(), "destination is null")) {
            if (isZip(file)) {
                Path pathToUnzip = destination.toPath().resolve(file.getName().substring(0, file.getName().indexOf('.')));
                Files.createDirectories(pathToUnzip);
                unzipRecursively(file, pathToUnzip.toFile());
            }
        }
    }

    /**
     * Проверяет, является ли файл zip
     *
     * @param file
     * @return
     */
    public static boolean isZip(File file) {
        if (file.isDirectory()) return false;
        try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {
            return raf.readInt() == 0x504B0304;
        } catch (IOException e) {
            log.error(e.getMessage());
        }
        return false;
    }

}
