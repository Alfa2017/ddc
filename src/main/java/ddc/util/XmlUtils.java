package ddc.util;

import lombok.extern.slf4j.Slf4j;
import org.xml.sax.SAXException;
import ddc.exception.AppDdsException;
import ddc.exception.ExceptionMessages;
import ddc.exception.ValidationDdsException;

import javax.xml.bind.*;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

@Slf4j
public class XmlUtils {

    private XmlUtils() {
    }

    public static void exportObject(JAXBElement element, String fileName) {
        try {
            JAXBContext context = JAXBContext.newInstance(element.getDeclaredType());
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            File file = new File(fileName);
            marshaller.marshal(element, file);
            log.debug("Сохранил объект в файл: {}", fileName);
        } catch (JAXBException e) {
            log.error("Не удалось экспортировать файл '{}': {}", fileName, e.getLinkedException().getLocalizedMessage());
        }
    }

    public static <T> void exportObject(T element, File file, String pathToSchema) throws AppDdsException, FileNotFoundException {
        exportObject(element, new FileOutputStream(file), pathToSchema);
    }

    public static <T> void exportObject(T element, OutputStream os, String pathToSchema) throws AppDdsException {
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(element.getClass());
            Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
            jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            URL schemaUrl = XmlUtils.class.getResource(pathToSchema);
            if (schemaUrl != null) {
                SchemaFactory schemaFactory = SchemaFactory.newInstance("http://www.w3.org/XML/XMLSchema/v1.1");
                Schema schema = schemaFactory.newSchema(schemaUrl);
                jaxbMarshaller.setSchema(schema);
            } else {
                log.error("Не удалось найти схему по пути {}", pathToSchema);
                throw new FileNotFoundException(ExceptionMessages.CANT_FIND_SCHEMA_BY_WAY.getMessage(pathToSchema));
            }
            jaxbMarshaller.marshal(element, os);
        } catch (MarshalException ue) {
            log.error("Ошибка валидации: {}", ExceptionUtils.getMessage(ue)/*ue.getMessage() == null ? ue.getLinkedException().toString() : ue.getMessage()*/);
            throw new AppDdsException(ExceptionMessages.ERROR_XSD_VALIDATION_FILE.getMessage());
        } catch (JAXBException | FileNotFoundException | SAXException e) {
            log.error("Не удалось экспортировать документ: {}", e.getMessage());
            throw new AppDdsException(ExceptionMessages.CANT_EXPORT_DOCUMENT.getMessage(e));
        }
    }

    public static void exportObject(Object object, OutputStream stream, JAXBElement element) {
        try {
            JAXBContext context = JAXBContext.newInstance(String.class, object.getClass());
            JAXBIntrospector introspector = context.createJAXBIntrospector();
            Marshaller marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
            if (null == introspector.getElementName(object)) {
                marshaller.marshal(element, stream);
            } else {
                marshaller.marshal(object, stream);
            }
        } catch (JAXBException e) {
            log.error("Не удалось экспортировать сущность '{}': ", object, e.getLinkedException().getLocalizedMessage());
        }
    }

    /**
     * Десериализует xml документ.
     * Используется если документ НЕ имеет корневого тэга
     *
     * @param file
     * @param pathToSchema
     * @param target
     * @param <T>
     * @return
     * @throws FileNotFoundException
     * @throws JAXBException
     * @throws SAXException
     */
    public static <T> T importObject(File file, String pathToSchema, Class target)
            throws ValidationDdsException {
        return (T) ((JAXBElement) (importObjectBase(file, pathToSchema, target))).getValue();
    }

    /**
     * Десериализует xml документ.
     * Используется если документ имеет корневой тэг
     *
     * @param file
     * @param pathToSchema
     * @param target
     * @param <T>
     * @return
     * @throws FileNotFoundException
     * @throws JAXBException
     * @throws SAXException
     */
    public static <T> T importObjectWithRoot(File file, String pathToSchema, Class target)
            throws ValidationDdsException {
        return (T) importObjectBase(file, pathToSchema, target);
    }

    private static Object importObjectBase(File file, String pathToSchema, Class target) throws ValidationDdsException {
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(target);
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            setSchema(jaxbUnmarshaller, pathToSchema);

            return jaxbUnmarshaller.unmarshal(file);
        } catch (UnmarshalException ue) {
            log.warn("Файл '{}' не проходит валидацию по XSD: {}", file.getAbsolutePath(), ue.getMessage() == null ? ue.getLinkedException().toString() : ue.getMessage());
            throw new ValidationDdsException(ExceptionMessages.ERROR_XSD_VALIDATION_FILE.getMessage());
        } catch (JAXBException | FileNotFoundException | SAXException e) {
            log.warn("Не удалось импортировать файл '{}': {}", file.getAbsolutePath(), e.getMessage());
            throw new ValidationDdsException(ExceptionMessages.CANT_IMPORT_FILE.getMessage(e));
        }
    }

    public static <T> T importObject(Object obj, String pathToSchema, Class target) throws ValidationDdsException {
        try {

            JAXBContext jaxbContext = JAXBContext.newInstance(target);//2

            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();

            setSchema(jaxbUnmarshaller, pathToSchema);


            if (obj instanceof File) {
                return (T) jaxbUnmarshaller.unmarshal((File) obj);
            } else if (obj instanceof InputStream) {
                return (T) jaxbUnmarshaller.unmarshal((InputStream) obj);
            } else {
                throw new ValidationDdsException("Неподдерживаемый формат файла");
            }
        } catch (UnmarshalException ue) {
            log.warn("Файл не проходит валидацию по XSD: {}", ue.getMessage() == null ? ue.getLinkedException().toString() : ue.getMessage());
            throw new ValidationDdsException(ExceptionMessages.ERROR_XSD_VALIDATION_FILE.getMessage());
        } catch (JAXBException | FileNotFoundException | SAXException e) {
            log.warn("Не удалось импортировать файл : {}", e.getMessage());
            throw new ValidationDdsException(ExceptionMessages.CANT_IMPORT_FILE.getMessage(e));
        }
    }

    private static void setSchema(Unmarshaller jaxbUnmarshaller, String pathToSchema) throws SAXException, FileNotFoundException {
        URL schemaUrl = XmlUtils.class.getResource(pathToSchema);
        if (schemaUrl != null) {
            SchemaFactory schemaFactory = SchemaFactory.newInstance("http://www.w3.org/XML/XMLSchema/v1.1");
            Schema schema = schemaFactory.newSchema(schemaUrl);
            jaxbUnmarshaller.setSchema(schema);
        } else {
            log.error("Не удалось найти схему по пути {}", pathToSchema);
            throw new FileNotFoundException(ExceptionMessages.CANT_FIND_SCHEMA_BY_WAY.getMessage(pathToSchema));
        }
    }

    public static boolean isXml(Path path) throws IOException {
        String mimeType = Files.probeContentType(path);
        return "application/xml".equals(mimeType) || "text/xml".equals(mimeType);
    }
}
