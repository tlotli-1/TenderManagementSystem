
package util;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Date utility for deadlines.
 */
public class DateUtil {
    
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    
    public static Timestamp now() {
        return Timestamp.valueOf(LocalDateTime.now());
    }
    
    public static Timestamp parse(String dateTimeStr) {
        LocalDateTime ldt = LocalDateTime.parse(dateTimeStr, FORMATTER);
        return Timestamp.valueOf(ldt);
    }
    
    public static boolean isPastDeadline(Timestamp deadline) {
        return deadline != null && deadline.before(now());
    }
    
    public static String format(Timestamp ts) {
        return ts.toLocalDateTime().format(FORMATTER);
    }
}
