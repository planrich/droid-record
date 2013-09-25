package at.pasra.record;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by rich on 9/15/13.
 */
public class SQLiteConverter {
    private static SimpleDateFormat sqliteDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    public static String dateToString(Date date) {
        if (date == null) {
            return null;
        }

        return sqliteDateFormat.format(date);
    }

    public static Date stringToDate(String string) {
        try {
            return sqliteDateFormat.parse(string);
        } catch (ParseException e) {
            return null;
        }
    }
}
