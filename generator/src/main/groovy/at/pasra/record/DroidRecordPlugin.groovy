package at.pasra.record

import at.pasra.record.generation.CodeGenerator
import at.pasra.record.task.GenerateMigrationTask
import at.pasra.record.task.MigrateTask
import org.gradle.api.InvalidUserDataException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.logging.Logging

class DroidRecordPlugin implements Plugin<Project> {

    public static String VERSION = "0.1.4"

    static def project;
    static def logger = Logging.getLogger(DroidRecordPlugin.class)

    public static void write(String path, String file, String content, boolean  forceCreate) {
        File fh = project.file("${path}")
        fh.mkdirs();
        fh = new File(fh, file)
        logger.info("generating ${fh.path}")

        if (forceCreate || !fh.exists()) {
            fh.write(content)
        }
    }

    public static void writeJavaSource(String path, String pkg, String file, String content, boolean forcecreate) {
        File fh = project.file("src/main/java/${pkg.replace('.','/')}")
        fh.mkdirs();
        fh = project.file("src/main/java/${pkg.replace('.','/')}/${file}")
        logger.info("generating ${fh.path}")

        if (forcecreate || !fh.exists()) {
            fh.write(content)
        }
    }

    @Override
    void apply(Project project) {

        DroidRecordPlugin.project = project;

        project.extensions.create("droid_record", DroidRecordPluginExtention)

        def migrate = project.task('migrate', type: MigrateTask)
        migrate.group = "Build"
        migrate.description = "Generates java source code for the database structure"

        def migration = project.task('migration', type: GenerateMigrationTask)
        migration.description = "Create a new migration file to transform the database"

        project.afterEvaluate {
            project.tasks.preBuild.dependsOn migrate
        }
    }

    static void sanitizeConfiguration(Project project) {

        DroidRecordPluginExtention ext = project.droid_record;

        File migPath = project.file(ext.migration_path)
        if (!migPath.exists()) {
            migPath.mkdirs();
        }

        File relationShip = new File(migPath, ext.relationship)
        if (!relationShip.exists()) {
            CodeGenerator c = new CodeGenerator(2, "dr");
            c.line("// Insert your model relationships here! See http://record.pasra.at/droid_record#relations")
            OutputStreamWriter w = new OutputStreamWriter(new FileOutputStream(relationShip));
            w.write(c.toString())
            w.close()
        }

        File sourcePath = project.file(ext.output_path)
        if (!sourcePath.isDirectory()) {
            throw new InvalidUserDataException("Output path '${sourcePath.path}' does not exist! Cannot proceed. Create the path manually or corret typos")
        }

        if (ext.output_package == null) {
            throw new InvalidUserDataException("Output package not specified. Please add output_package=your.package to the droid record plugin!")
        }

        if (ext.domain_package == null) {
            ext.domain_package = ext.output_package
        }
    }

    static File dir(path, pkg) {
        File file = new File(path.toString());
        pkg.toString().split(/\./).each { folder ->
            file = new File(file, folder)
        }
        return file;
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


