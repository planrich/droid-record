package at.pasra.record.remote;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.InputMismatchException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by rich on 22.11.14.
 */
public class RouteUtil {

    public static String resolveDSL(String route, Object ref) {
        int pos = 0;
        Pattern pattern = Pattern.compile("\\{([^}]+)\\}");
        Matcher match = pattern.matcher(route);
        StringBuilder str = new StringBuilder();
        while (match.find()) {
            String field = match.group(1);
            str.append(route.substring(pos, match.start()));
            pos = match.end();
            String method = "toString";
            if (field.contains("#")) {
                String[] split = field.split("#");
                field = split[0];
                method = split[1];
                if (split.length > 2) {
                    throw new IllegalArgumentException("only one method invocation is supported");
                }
            }

            try {
                Field f = ref.getClass().getDeclaredField(field);
                boolean accessible = f.isAccessible();
                f.setAccessible(true);
                Object obj = f.get(ref);

                Method m = obj.getClass().getMethod(method);
                Object result = m.invoke(obj);

                str.append(result);

                f.setAccessible(accessible);
            } catch (NoSuchFieldException e) {
                throw new RemoteException(e);
            } catch (IllegalAccessException e) {
                throw new RemoteException(e);
            } catch (NoSuchMethodException e) {
                throw new RemoteException(e);
            } catch (InvocationTargetException e) {
                throw new RemoteException(e);
            }
        }

        if (pos < route.length()) {
            str.append(route.substring(pos, route.length()));
        }

        return str.toString();
    }
}
