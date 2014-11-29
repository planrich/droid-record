package at.pasra.record.task

import at.pasra.record.DroidRecordPlugin
import at.pasra.record.DroidRecordPluginExtention
import at.pasra.record.generation.CodeGenerator
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

/**
 * Created by rich on 9/13/13.
 */
class GenerateMigrationTask extends DefaultTask {

    def MIGRATION_REGEX = /^(\d+)_([^.]*).dr$/

    @TaskAction
    void taskExec() {

        DroidRecordPlugin.sanitizeConfiguration(project)

        def name = System.getProperty("name", "migration").replaceAll(" ","_")

        DroidRecordPluginExtention ex = project.droid_record;
        File root = project.file(ex.migration_path)

        def max = 1.toLong();
        root.list().each{ a ->
            def matcher = a =~ MIGRATION_REGEX
            if (matcher.matches()) {
                def version = matcher.group(1).toLong()
                if (version > max) {
                    max = version;
                }
            }
        }

        File migration = new File(root, "${max}_${name}.dr")
        OutputStreamWriter w = new OutputStreamWriter(new FileOutputStream(migration));
        CodeGenerator c = new CodeGenerator();
        c.line("// create_table {")
        c.line("//   name 'lion'")
        c.line("//   fields {")
        c.line("//     cage 'long'")
        c.line("//   }")
        c.line("// }")

        w.write(c.toString());
        w.close()
    }

    String timestamp(Date when) {
        return when.format("yyyyMMddHHmmss")
    }
}
