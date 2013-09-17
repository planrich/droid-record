package com.pasra.android.record

/**
 * Created by rich on 9/8/13.
 */
class Inflector {

    static String singularize(String name) {
        if (name.startsWith('#')) {
            return name.substring(1)
        }

        // that's it for now. consider using inflector from rails as this happens during pre compile time
        // performance is not necessery at any cost
        // https://github.com/rails/rails/blob/feaa6e2048fe86bcf07e967d6e47b865e42e055b/activesupport/lib/active_support/inflections.rb
        return name;
    }

    static String tabelize(String name) {
        StringBuilder builder = new StringBuilder();

        boolean nextScore = false;
        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);

            if (Character.isUpperCase(c) && i != 0) {
                builder.append('_')
            }
            builder.append(Character.toLowerCase(c));
        }

        return builder.toString();
    }

    static String camelize(String name) {
        StringBuilder builder = new StringBuilder();

        boolean nextUp = true;
        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            if (c == '_') {
                nextUp = true;
                continue;
            }
            if (nextUp) {
                builder.append(Character.toUpperCase(c));
                nextUp = false;
            } else {
                builder.append(c);
            }

        }

        return builder.toString()
    }
}
