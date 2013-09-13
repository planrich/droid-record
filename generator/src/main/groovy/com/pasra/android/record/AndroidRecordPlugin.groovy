package com.pasra.android.record

import com.pasra.android.record.generation.CodeGenerator
import org.gradle.api.InvalidUserDataException
import org.gradle.api.Plugin
import org.gradle.api.Project

class AndroidRecordPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {

        project.extensions.create("android_record", AndroidRecordPluginExtention)

        project.tasks.create('migrate', MigrateTask)
        // move it to a ruby gem? really inflexible param passing in gradle
        project.tasks.create('migration', GenerateMigrationTask)

    }

    static void sanitizeConfiguration(Project project) {

        AndroidRecordPluginExtention ext = project.android_record;

        File migPath = project.file(ext.migration_path)
        if (!migPath.exists()) {
            migPath.mkdirs();
        }

        File relationShip = new File(migPath, ext.relationship)
        if (!relationShip.exists()) {
            CodeGenerator c = new CodeGenerator(2, "json");
            c.wrap() {
                c.line("comment: 'Insert your model relationships here! See the documentation for details.',")
            }
            OutputStreamWriter w = new OutputStreamWriter(new FileOutputStream(relationShip));
            w.write(c.toString())
            w.close()
        }

        File sourcePath = project.file(ext.output_path)
        if (!sourcePath.exists()) {
            throw new InvalidUserDataException("Output path '${migPath.path}' does not exist! Cannot proceed. Create the path manually or corret typos")
        }

        if (ext.output_package == null) {
            throw new InvalidUserDataException("Output package not specified. Please add output_package=your.package to the android record plugin!")
        }

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


