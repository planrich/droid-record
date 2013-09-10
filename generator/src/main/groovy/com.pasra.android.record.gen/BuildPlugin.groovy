package com.pasra.android.record.gen


import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.gson.stream.JsonReader
import org.gradle.api.Plugin
import org.gradle.api.Project

class BuildPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        project.task('migrate') << {
            File root = project.file('src/main/record');
            JsonParser parser = new JsonParser();

            MigrationContext ctx = new MigrationContext(project.file('src/main/java').path, 'com.pasra.android.record.sample');

            root.list().each { String file ->
                JsonReader reader = new JsonReader(new StringReader(new File(root, file).text));
                reader.setLenient(true);
                JsonObject obj = parser.parse(reader).getAsJsonObject();
                ctx.addMigrationStep(obj, new File(root, file), Util.versionOfFile(file));
            }

            ctx.generate()
        }
    }

}


