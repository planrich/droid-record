package at.pasra.record

import at.pasra.record.generation.CodeGenerator
import org.gradle.api.InvalidUserDataException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.logging.Logging

/*!
 * @getting_started Getting Started
 * -#position 1
 * .alert.alert-info
 *   %span{ style: 'font-size: 25px;' } &beta;-phase.
 *   %br
 *   Please report bugs or provide feedback to 'rich [at] pasra [dot] at'.
 * %p
 *   If you used active record already you might find yourself
 *   very comfortable with this toolkit. Note that this is not an
 *   approach to clone active record as it would not make sense.
 *   After all ruby is a dynamic language moving a lot of it's functionality
 *   into the runtime. Java in this respect makes those things not that easy.
 *   Android is a mobile operating system and runs most likely on battery powered
 *   devices. Droid Record keeps that in mind and helps you to spend your time
 *   on the important stuff of your app, the business logic.
 *
 *
 * %h3 Changelog
 *
 * Version 0.0.7: Added double type
 * %br
 * Version 0.0.6: First Version
 *
 * %h3 Definitions
 * %p
 *   In this documentation a
 *   %span.migration-ref record object
 *   is the java object representation of a row in a table.
 *
 * %h3 Naming convention
 *
 * %p
 *   When providing a name in a json file, you should always provide a singular name.
 *   If you want to create a object called 'User' you should specify 'user' in the name field, not
 *   'users'.
 * %p
 *   The table names saved into the sqlite database are pluralized, thus the java User class is
 *   saved into the 'users' sqlite table.
 *
 * @getting_started|build_file Gradle
 *
 * %p
 *   Droid Record ({?class:arname;DR}) is tightly integrated into the new Android tool chain.
 *   The next code block shows how to integrate {?class:arname;DR} into the gradle build system.
 *
 * %span.filename build.gradle
 * %pre
 *   %code{ data: { language: 'javascript' } }
 *     :preserve
 *       buildscript {
 *         repositories {
 *            mavenCentral()
 *            maven { url "http://record.pasra.at/public/repo" }
 *         }
 *         dependencies {
 *            classpath 'com.google.code.gson:gson:2.2.+'
 *            classpath 'at.pasra.record:generator:0.0.+'
 *         }
 *       }
 *
 *       repositories {
 *         mavenCentral()
 *         maven { url "http://record.pasra.at/public/repo" }
 *       }
 *
 *       dependencies {
 *         compile 'at.pasra.record:library:0.0.+@aar'
 *       }
 *
 *       apply plugin: 'droid_record'
 *
 *       droid_record {
 *         output_package='com.example.sample.generate'
 *       }
 * %p
 *   By applying the plugin 'droid_record'
 *   %a{ href: '#commands' } two tasks
 *   gradle tasks are added to your project. Additionally every time you compile your project
 *   the migration will be automatically generated.
 * %p
 *   Droid Record has it's own configuration block where you
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
 *     If the migration numbering is not given, Droid Record cannot ensure that the generated code is correct
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
 * @getting_started|generated_files The first table
 * -#after getting_started|commands
 * %p
 *   {?class:arname;AD} uses json files to describe you database objects. To create your first
 *   table you can do the following:
 *
 * %pre
 *   %code{ 'data-language' => 'dsl' }
 *     :preserve
 *       change: [
 *         { cmd: create_table,
 *           name: 'picture',
 *           fields: {
 *             image: 'blob'
 *           }
 *         }
 *       ]
 *
 * %p Droid Record generates the following files:
 * %pre
 *   :preserve
 *     com.example.generate
 *       ├── AbstractPicture.java
 *       ├── Picture.java
 *       ├── PictureRecord.java
 *       ├── PictureRecordBuilder.java
 *       ├── LocalSession.java
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
 *     This file will never be overwritten by Droid Record, thus making it a good place
 *     to customize your record.
 *
 * @getting_started|gs_examples Examples
 * -#after getting_started|generated_files
 *
 * %p
 *   One possibility and in most cases the most suitable for many applications is
 *   to create a Application subclass.
 *
 * %pre
 *   %code{ 'data-language' => 'java' }
 *     :preserve
 *
 *       public class CustomApp extends Application {
 *
 *           private SQLiteDatabase db;
 *           private LocalSession session;
 *
 *           @Override
 *           public void onCreate() {
 *              db = openOrCreateDatabase("name", MODE_PRIVATE, null);
 *              new RecordMigrator(db).migrate();
 *              session = new LocalSession(db);
 *           }
 *
 *           public LocalSession getSession() {
 *               return session;
 *           }
 *       }
 *
 * %pre
 *   %code{ 'data-language' => 'java' }
 *     :preserve
 *       LocalSession session = ((CustomApp)getApplication()).getSession();
 *
 *       Picture picture = new Picture();
 *       picture.setImage(new byte[] { (byte)0xca, (byte)0xfe, (byte)0xba, (byte)0xbe });
 *       session.savePicture(picture); // save is insert or update at the same time.
 *       session.savePicture(picture); // this second call will invoke update
 *       long id = picture.getId();
 *       Picture picCopy = session.findPicture(id);
 *       session.destroyPicture(picCopy);
 *       picture = null;
 */
class DroidRecordPlugin implements Plugin<Project> {

    public static String VERSION = "0.0.6"

    static def outputFiles = []
    static def project;
    static def logger = Logging.getLogger(DroidRecordPlugin.class)

    public static void write(String path, String pkg, String file, String content, boolean forcecreate) {
        File fh = project.file("src/main/java/${pkg.replace('.','/')}")
        fh.mkdirs();
        fh = project.file("src/main/java/${pkg.replace('.','/')}/${file}")
        logger.info("generating ${fh.path}")

        fh.write(content)
    }

    @Override
    void apply(Project project) {

        project.extensions.create("droid_record", DroidRecordPluginExtention)

        def migrate = project.task('migrate', type: MigrateTask, )
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
            CodeGenerator c = new CodeGenerator(2, "json");
            c.line("comment: 'Insert your model relationships here! See http://recrod.pasra.at/droid_record#relations.'")
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


