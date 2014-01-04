package at.pasra.record

import at.pasra.record.generation.CodeGenerator
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

/**
 * Created by rich on 9/13/13.
 */
class GenerateMigrationTask extends DefaultTask {

    @TaskAction
    void taskExec() {

        DroidRecordPlugin.sanitizeConfiguration(project)

        def name = System.getProperty("name", "migration").replaceAll(" ","_")

        File root = project.file(project.android_record.migration_path)

        File migration = new File(root, "${timestamp(new Date())}_${name}.json")
        OutputStreamWriter w = new OutputStreamWriter(new FileOutputStream(migration));
        CodeGenerator c = new CodeGenerator();
        c.line("change: [")
        c.line("")
        c.line("]")

        w.write(c.toString());
        w.close()
    }

    String timestamp(Date when) {
        return when.format("yyyyMMddHHmmss")
    }
}
