package at.pasra.record.remote.serialize;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import java.lang.reflect.Field;
import java.util.Date;
import java.util.Map;

/**
 * Created by rich on 13.11.14.
 */
public abstract class BaseSerializer<T> {

    private T target;
    private Map<String,String> remoteResolutionMap;

    public BaseSerializer() {
        remoteResolutionMap = getRemoteResolutionMap();
    }

    protected abstract Map<String, String> getRemoteResolutionMap();
    protected abstract T newObject();

    public T deserialize(JsonObject object) {
        setTarget(newObject());
        for (Map.Entry<String,String> entry : remoteResolutionMap.entrySet()) {
            Object typed = getTyped(entry.getValue(), entry.getKey(), object);
            if (typed != null) {
                setObjectProperty(entry.getValue(), typed);
            }
        }
        return target;
    }

    public JsonObject serialize(T target) {
        setTarget(target);
        JsonObject object = new JsonObject();
        for (Map.Entry<String, String> entry : remoteResolutionMap.entrySet()) {
            object.add(entry.getKey(), getObjectProperty(entry.getValue()));
        }
        return object;
    }

    /**
     * Resolves
     * @param key
     * @param object
     * @return
     */
    private Object getTyped(String objKey, String key, JsonObject object) {
        if (!object.has(key)) {
            return null;
        }

        Field field = getField(target.getClass(), objKey);
        Class<?> clazz = field.getType();
        if (clazz == Date.class) {
            throw new SerializeException("unkown type");
        } else if (clazz == Integer.class) {
            return object.getAsJsonPrimitive(key).getAsInt();
        } else if (clazz == Long.class) {
            return object.getAsJsonPrimitive(key).getAsLong();
        } else if (clazz == Double.class) {
            return object.getAsJsonPrimitive(key).getAsDouble();
        } else if (clazz == Float.class) {
            return object.getAsJsonPrimitive(key).getAsFloat();
        } else if (clazz == Boolean.class) {
            return object.getAsJsonPrimitive(key).getAsBoolean();
        } else if (clazz == String.class) {
            return object.getAsJsonPrimitive(key).getAsString();
        } else {
            throw new SerializeException("unkown type: " + key + " obj property name " + objKey);
        }
    }

    public void setObjectProperty(String property, Object value) {
        try {
            Field field = getField(target.getClass(), property);
            boolean accessible = field.isAccessible();
            field.setAccessible(true);
            field.set(target, value);
            field.setAccessible(accessible);
        } catch (IllegalAccessException e) {
            throw new SerializeException(e);
        }
    }

    public JsonElement getObjectProperty(String property) {
        try {
            Field field = target.getClass().getField(property);
            boolean accessible = field.isAccessible();
            field.setAccessible(true);
            Object value = field.get(target);
            field.setAccessible(accessible);

            if (value instanceof Number) {
                return new JsonPrimitive((Number)value);
            } else {
                return new JsonPrimitive(value.toString());
            }

        } catch (NoSuchFieldException e) {
            throw new SerializeException(e);
        } catch (IllegalAccessException e) {
            throw new SerializeException(e);
        }
    }

    public Field getField(Class<?> clazz, String name) {
        try {
            return clazz.getDeclaredField(name);
        } catch (NoSuchFieldException e) {
            if (clazz.getSuperclass() != null) {
                return getField(clazz.getSuperclass(), name);
            }
            throw new SerializeException(e);
        }
    }

    public void setTarget(T target) {
        this.target = target;
    }
}
