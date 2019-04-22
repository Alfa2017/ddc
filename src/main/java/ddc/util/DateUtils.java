package ddc.util;

import lombok.extern.slf4j.Slf4j;
import ddc.exception.ExceptionMessages;
import ddc.exception.ServerErrorDdsException;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.GregorianCalendar;

@Slf4j
public class DateUtils {

    public static final ZoneId ZONE_ID = ZoneId.of("Europe/Moscow");

    public static XMLGregorianCalendar toGregorianCalendar(Date date) throws ServerErrorDdsException {
        GregorianCalendar gregorianCalendar = new GregorianCalendar();
        gregorianCalendar.setTime(date);
        return transform(gregorianCalendar);
    }

    public static XMLGregorianCalendar toGregorianCalendar(int year, int month, int date) throws ServerErrorDdsException {
        GregorianCalendar gregorianCalendar = new GregorianCalendar();
        gregorianCalendar.set(year, month, date);
        return transform(gregorianCalendar);
    }

    public static XMLGregorianCalendar toGregorianCalendar(LocalDate localDate) throws ServerErrorDdsException {
        if (localDate == null) return null;
        GregorianCalendar gcal = GregorianCalendar.from(localDate.atStartOfDay(ZoneId.systemDefault()));
        try {
            return DatatypeFactory.newInstance().newXMLGregorianCalendar(gcal);
        } catch (Exception e) {
            throw new ServerErrorDdsException(ExceptionMessages.FAILED_CONVERT_DATE_TO_XML.getMessage(localDate));
        }
    }

    public static XMLGregorianCalendar getCurrentDate(){
        GregorianCalendar gregorianCalendar = new GregorianCalendar();
        try {
            DatatypeFactory datatypeFactory = DatatypeFactory.newInstance();
            return datatypeFactory.newXMLGregorianCalendar(gregorianCalendar);
        } catch (DatatypeConfigurationException e) {
            log.error("Не удалось получить текущую дату ", e);
            return null;
        }
    }

    private static XMLGregorianCalendar transform(GregorianCalendar gregorianCalendar) throws ServerErrorDdsException {
        try {
            return DatatypeFactory.newInstance().newXMLGregorianCalendar(gregorianCalendar);
        } catch (DatatypeConfigurationException e) {
            throw new ServerErrorDdsException(e.getMessage());
        }
    }

    public static XMLGregorianCalendar toGregorianCalendar(Long epochSeconds) throws ServerErrorDdsException {
        return toGregorianCalendar(new Date(epochSeconds * 1000));
    }


    public static LocalDate toLocalDate(XMLGregorianCalendar opMinDate) {
        if (opMinDate == null) return null;
        return opMinDate.toGregorianCalendar().toZonedDateTime()
                .withZoneSameInstant(ZoneId.of("Europe/Moscow")).toLocalDate();
    }

    public static LocalDate toLocalDate(Long longValue) {
        if (longValue == null) longValue = 0L;
        return Instant.ofEpochSecond(longValue).atZone(ZONE_ID).toLocalDate();
    }


    public static LocalDateTime toLocalDateTime(Date dateToConvert) {
        return dateToConvert.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
    }

    public static LocalDateTime toLocalDateTime(Long longValue) {
        if (longValue == null) longValue = 0L;
        return Instant.ofEpochSecond(longValue).atZone(ZONE_ID).toLocalDateTime();
    }

    public static LocalDateTime toLocalDateTime(XMLGregorianCalendar calendar) {
        if (calendar == null) return null;
        return calendar.toGregorianCalendar().toZonedDateTime().toLocalDateTime();
    }

    public static LocalDateTime currentLocalDateTime() {
        return LocalDateTime.now(ZONE_ID);
    }


    public static Long toSeconds(LocalDate localDate) {
        if (localDate == null) return 0L;
        return localDate.atStartOfDay(ZONE_ID).toEpochSecond();
    }

    public static Long toSeconds(LocalDateTime localDateTime) {
        if (localDateTime == null) return 0L;
        return localDateTime.atZone(ZONE_ID).toEpochSecond();
    }

    public static Long toSeconds(XMLGregorianCalendar calendar) {
        if (calendar == null) return 0L;
        return calendar.toGregorianCalendar().toInstant().getEpochSecond();
    }

    public static Long toSeconds(Date date) {
        if (date == null) return 0L;
        return date.toInstant().atZone(ZoneId.systemDefault()).toEpochSecond();
    }


    public static String formatDate(Date date, String pattern) {
        SimpleDateFormat formatter = new SimpleDateFormat(pattern);
        return formatter.format(date);
    }

}
