package com.pasra.android.record.gen

/**
 * Created by rich on 9/8/13.
 */
class Util {

    static int versionOfFile(String file) {
        return Integer.parseInt(file.split("_")[0]);
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
            if (nextUp) {
                builder.append(Character.toUpperCase(c));
                nextUp = false;
            } else {
                builder.append(c);
            }

            if (c == '_') {
                nextUp = true;
            }
        }

        return builder.toString()
    }

    static File file(path, pkg, name, boolean create) {
        File file = new File(path.toString());
        pkg.toString().split(/\./).each { folder ->
            file = new File(file, folder)
        }

        if (!file.exists() && create) {
            file.mkdirs();
        }

        file = new File(file, name.toString());
        if (create) {
            file.deleteOnExit();
        }

        return file;
    }
}
