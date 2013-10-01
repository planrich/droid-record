package at.pasra.record

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.google.gson.stream.JsonReader
import at.pasra.record.generation.MigrationContext
import org.gradle.api.DefaultTask
import org.gradle.api.InvalidUserCodeException
import org.gradle.api.InvalidUserDataException
import org.gradle.api.tasks.TaskAction

/**
 * Created by rich on 9/13/13.
 */
class MigrateTask extends DefaultTask {

    def migrationRegex = /^(\d+)_([^.]*).json$/

    @TaskAction
    void taskExec() {

        AndroidRecordPlugin.sanitizeConfiguration(getProject())

        AndroidRecordPluginExtention ex = project.android_record;

        File root = project.file(ex.migration_path);
        JsonParser parser = new JsonParser();

        MigrationContext ctx = new MigrationContext(project.file(ex.output_path).path, ex.output_package);

        String[] files = root.list().sort()
        files.each { String file ->
            if (isMigration(file)) {
                JsonReader reader = new JsonReader(new StringReader("{"+(new File(root, file).text)+ "}"));
                reader.setLenient(true);
                try {
                    JsonObject obj = parser.parse(reader).getAsJsonObject();

                    long version = extractVersion(file)
                    ctx.addMigrationStep(obj, new File(root, file), version);
                } catch (Exception e) {
                    throw new InvalidUserCodeException("In file ${file}: " + e.message); // + e.stackTrace.take(15).collect({ StackTraceElement s -> s.toString() }).join("\n    "))
                }
            } else {
                logger.info("Did not parse json file '${file}' in migration path!")
            }
        }

        File relationShip = new File(root, ex.relationship);
        if (relationShip.exists()) {
            JsonReader reader = new JsonReader(new StringReader("{"+relationShip.text+"}"));
            reader.setLenient(true);
            try {
                JsonObject obj = parser.parse(reader).getAsJsonObject();
                ctx.relations(obj);
            } catch (Exception e) {
                throw new InvalidUserCodeException("In file ${ex.relationship}: " + e.message)
            }
        } else {
            logger.info("could not find relation ship file '${ex.relationship}' in migration path")
        }

        ctx.generate()
    }

    /**
     * Extract the version of a file like 12345_name.json
     * @param filename
     * @return the version
     * @throws InvalidUserDataException If the filename is malformed
     */
    long extractVersion(String filename) {
        def matcher = filename =~ migrationRegex
        if (!matcher.matches()) {
            throw InvalidUserDataException("The migration ${filename} does not match the regex '${migrationRegex}'!");
        }
        return Long.parseLong(matcher.group(1))
    }

    /**
     * Check if the file is a migration file.
     * @param file
     */
    boolean isMigration(String file) {
        if (file ==~ migrationRegex) {
            return true;
        }

        return false;
    }
}
