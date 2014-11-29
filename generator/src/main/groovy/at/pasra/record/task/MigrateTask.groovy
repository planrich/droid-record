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

    def MIGRATION_REGEX = /^(\d+)_([^.]*).dr$/
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
        def output_path = project.file(ex.output_path).path
        def output_pkg = ex.output_package
        def domain_pkg = ex.domain_package
        context = new MigrationContext(output_path, output_pkg, domain_pkg);

        File root = project.file(ex.migration_path);

        String[] files = root.list().sort{ a,b ->
                    def matcher = a =~ MIGRATION_REGEX
                    if (!matcher.matches()) { return 1; }
                    def ai = matcher.group(1).toLong()
                    matcher = b =~ MIGRATION_REGEX
                    if (!matcher.matches()) { return -1; }
                    def bi = matcher.group(1).toLong()

                    return ai <=> bi
                }
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

        context.generateCurrentSchema(project.file(ex.migration_path).path, ex.schema);
    }

    /**
     * Extract the version of a file like 12345_name.json
     * @param filename
     * @return the version
     * @throws InvalidUserDataException If the filename is malformed
     */
    long extractVersion(String filename) {
        def matcher = filename =~ MIGRATION_REGEX
        if (!matcher.matches()) {
            throw InvalidUserDataException("The migration ${filename} does not match the regex '${MIGRATION_REGEX}'!");
        }
        return Long.parseLong(matcher.group(1))
    }

    /**
     * Check if the file is a migration file.
     * @param file
     */
    boolean isMigration(String file) {
        if (file ==~ MIGRATION_REGEX) {
            return true;
        }

        return false;
    }
}
