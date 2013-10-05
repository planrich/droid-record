package at.pasra.record

import at.pasra.record.generation.CodeGenerator
import org.gradle.api.InvalidUserDataException
import org.gradle.api.Plugin
import org.gradle.api.Project

/*!
 * @getting_started Getting Started
 * %p
 *   If you have use active record before you might find yourself
 *   very confortable with this toolkit. Note that this is not an
 *   approach to clone active record as it would make sence.
 *   After all ruby is a dynamic language moving alot of it's functionality
 *   into the runtime. Java in this respect makes those things not that easy.
 *   Android is a mobile operating system and runs most likely on battery powered
 *   devices. Android Record keeps that in mind and helps you to spend your time
 *   on the important stuff of your app, the business logic.
 *
 * %h3 Definitions
 * %p
 *   In this documentation a
 *   %span.migration-ref record object
 *   is the java object representation of a table in a sqlite database.
 *
 * @getting_started|build_file Gradle
 *
 * %p
 *   Android Record is tightly integrated into the new android toolchain.
 *   The next code block shows how to integrate AR into the gradle build system.
 *
 * %span.filename build.gradle
 * %pre
 *   %code{ data: { language: 'javascript' } }
 *     :preserve
 *       buildscript {¬
 *         repositories {¬
 *            maven { url "http://record.pasra.at/repo" }¬
 *         }¬
 *         dependencies {¬
 *            classpath 'at.pasra.record.generator:generator:0.0.+'¬
 *         }¬
 *       }¬
 *       ¬
 *       repositories {¬
 *         maven { url "http://record.pasra.at/repo" }¬
 *       }¬
 *       ¬
 *       dependencies {¬
 *         compile 'at.pasra.record:library:0.0.+@aar'¬
 *       }¬
 *
 *        apply plugin: 'android_record'¬
 *
 *       android_record {¬
 *         output_package='com.example.sample.generate'¬
 *       }¬
 * %p
 *   By applying the plugin 'android_record'
 *   %a{ href: '#commands' } two tasks
 *   gradle tasks are added to your project.
 * %p
 *   Android Record has it's own configuration block where you
 *   can specify the following parameters:
 * %dl
 *   %dt output_package
 *   %dd Required. The package of the generated classes. Example: 'com.example.generate'
 *   %dt migration_path
 *   %dd A path relative to the build.gradle file. In this folder the migration files and the relationship file will be searched. default: 'src/main/record'
 *   %dt relationship
 *   %dd The filename of the relationship file, default: 'relationship.json'
 *   %dt output_path
 *   %dd A path relative to the build.gradle file. The code will be generated to the package in this folder. Default: 'src/main/java'
 *
 * @getting_started|commands Commands
 * -#after getting_started|build_file
 *
 * %code $ gradle migration -Dname=something_your_remember
 * %p
 *   Generates a file in the migration_path of the following format: {timestamp}_{name_property}.json.
 *   You can also add this file by hand and use any number instead of the timestamp.
 *   .alert.alert-warning
 *     If you add the files by hand you should ensure that the files have increasing timestamps.
 *     If the migration numbering is not given, Android Record cannot ensure that the generated code is correct
 *     and in most cases will refuse to generate code.
 *
 * %hr
 *
 * %code $ gradle migrate
 * %p
 *   Loads the relationship and all migration files. Sanitizes the input and if the given input is correct
 *   produces the code.
 *   Additionally this command is idempotent. You can invoke it as many times as you like and it will generate
 *   the same output as long as the input stays the same.
 *
 * @getting_started|generated_files Generated Code
 * -#after getting_started|commands
 * %p
 *   Depending on the migrations in the project, java code will be generated. Given the following
 *   migration:
 *
 * %pre
 *   %code{ 'data-language' => 'javascript' }
 *     :preserve
 *       change: {
 *         create_table: {
 *           name: 'picture',
 *           fields: {
 *             image: 'blob'
 *           }
 *         }
 *       }
 * %p Android Record generates the following files:
 * %pre
 *   :preserve
 *     com.example.generate
 *       ├── AbstractPicture.java
 *       ├── LocalSession.java
 *       ├── Picture.java
 *       ├── PictureRecordBuilder.java
 *       ├── PictureRecord.java
 *       └── RecordMigrator.java
 * %p
 *   %span.migration-ref LocalSession.java
 *   ,
 *   %span.migration-ref RecordMigrator.java
 *   will always be generated.
 * %p
 *   Every table invokes the generation of three files. In the example above this would be
 *   %span.migration-ref AbstractPicture.java
 *   ,
 *   %span.migration-ref PicatureRecord.java
 *   ,
 *   %span.migration-ref Picture.java
 *   and
 *   %span.migration-ref PictureRecordBuilder.java
 *   \.
 * %p
 *   The
 *   %span.migration-ref Picture.java
 *   file is the file you can customize. It extends AbstractPicture.
 *   .alert.alert-info
 *     The table names java object will only be generated the first time you create the table.
 *     After that this file will never be overriden by Android Record, thus making it a good place
 *     to customize your record.
 */
class AndroidRecordPlugin implements Plugin<Project> {

    public static String VERSION = "0.0.4"

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
            c.line("comment: 'Insert your model relationships here! See http://recrod.pasra.at/android_record#relations.'")
            OutputStreamWriter w = new OutputStreamWriter(new FileOutputStream(relationShip));
            w.write(c.toString())
            w.close()
        }

        File sourcePath = project.file(ext.output_path)
        if (!sourcePath.isDirectory()) {
            throw new InvalidUserDataException("Output path '${sourcePath.path}' does not exist! Cannot proceed. Create the path manually or corret typos")
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


