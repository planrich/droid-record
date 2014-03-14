package at.pasra.record.task

import at.pasra.record.DroidRecordPlugin
import at.pasra.record.DroidRecordPluginExtention
import at.pasra.record.loading.MigrationContext
import org.gradle.api.DefaultTask
import org.gradle.api.InvalidUserDataException
import org.gradle.api.Task
import org.gradle.api.tasks.TaskAction

/**
 * Created by rich on 9/13/13.
 */
class MigrateTask extends DefaultTask {

    def migrationRegex = /^(\d+)_([^.]*).json$/
    def context;

    public MigrateTask() {
        super();

        doLast { Task t ->
            DroidRecordPlugin.project = t.project
            context.generate()
        }
    }

    @TaskAction
    void taskExec() {
        DroidRecordPluginExtention ex = project.droid_record;
        DroidRecordPlugin.sanitizeConfiguration(project)
        context = new MigrationContext(project.file(ex.output_path).path, ex.output_package);

        File root = project.file(ex.migration_path);

        String[] files = root.list().sort()
        files.each { String filename ->
            File file = new File(root, filename)
            if (file.isFile()) {
                if (isMigration(filename)) {
                    long version = extractVersion(filename)
                    context.addMigrationStep(file, version);
                } else {
                    if ( ! (filename.equals(ex.schema) || filename.equals(ex.relationship)) ) {
                        logger.warn("Did not parse file '${filename}' in migration path!")
                    }
                }
            }
        }

        File relationShip = new File(root, ex.relationship);
        if (relationShip.exists()) {
            context.relations(relationShip);
        } else {
            logger.info("could not find relation ship file '${ex.relationship}' in migration path")
        }
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
