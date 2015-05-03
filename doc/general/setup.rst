Setup
=====

Droid Record (DR) is tightly integrated into the new Android tool chain.
The next code block shows how to integrate DR into the gradle build system.


.. code-block:: groovy

    buildscript {
        repositories {
           mavenCentral()
           jcenter() // DR is located here
        }
        dependencies {
           classpath 'at.pasra.record:generator:0.1.4' // loads the plugin
        }
    }

    repositories {
        mavenCentral()
        jcenter() // DR is located here
    }

    dependencies {
        compile 'at.pasra.record:library:0.1.4@aar' // loads the droid record library
    }

    apply plugin: 'droid_record' // activates the plugin

    droid_record { // custom configuration for this project
        domain_package='com.example.sample.domain'
        output_package='com.example.sample.generate'
    }

By applying the plugin 'droid_record' two tasks gradle tasks are added to
your project. Additionally every time you compile your project the migration
will be automatically generated.

Droid Record has it's own configuration block where you
can specify the following parameters:

* **output_package**: (required) The package of the generated classes. Example: 'com.example.generate'
* **domain_package**: (required) The package for the Java classes you might want to extend with our code! Example: 'com.example.domain'
* **migration_path**: A path relative to the build.gradle file. In this folder the migration files and the relationship file will be searched. Default: 'src/main/record'
* **relationship**: The filename of the relationship file, default: 'relationship.dr'
* **output_path**: A path relative to the build.gradle file. The code will be generated to the package in this folder. Default: 'src/main/java'

Commands
--------

.. code-block:: shell

    $ gradle migration -Dname=something_your_remember

Generates a file in the migration_path of the following format: {revision}_{name}.dr.
You can also add this file by hand and use any number instead of the timestamp.

.. note::

    If you add the files by hand you should ensure that the files have increasing timestamps.
    If the migration numbering is not given, Droid Record cannot ensure that the generated code is correct
    and in most cases will refuse to generate code.

.. code-block:: shell

    $ gradle migrate

Loads the relationship and all migration files. Sanitizes the input and if the given input is correct
produces the code.
Additionally this command is idempotent. You can invoke it as many times as you like and it will generate
the same output as long as the input stays the same.

Creating the first table
------------------------

DR uses groovy files (ending dr) to describe you database objects.
To create your first table you can do the following:

.. code-block:: groovy

   create_table {
     name 'picture'
     fields {
       image 'blob'
     }
   }

Droid Record generates the following directory structure: 

.. code-block:: shell

    com/example/sample/generate
      ├── AbstractPicture.java
      ├── Picture.java
      ├── PictureRecord.java
      ├── PictureRecordBuilder.java
      ├── LocalSession.java
      └── RecordMigrator.java

.. 
    %p
      %span.migration-ref LocalSession.java
      ,
      %span.migration-ref RecordMigrator.java
      will always be generated.
    %p
      Every table invokes the generation of three files. In the example above this would be
      %span.migration-ref AbstractPicture.java
      ,
      %span.migration-ref PicatureRecord.java
      ,
      %span.migration-ref Picture.java
      and
      %span.migration-ref PictureRecordBuilder.java
      \.
    %p
      The
      %span.migration-ref Picture.java
      file is the file you can customize. It extends AbstractPicture.
      .alert.alert-info
        The table names java object will only be generated the first time you create the table.
        This file will never be overwritten by Droid Record, thus making it a good place
        to customize your record.


Create and migrate the Database
-------------------------------

One possibility and in most cases the most suitable for many applications is
to create a Application subclass.

.. code-block:: java

    public class CustomApp extends Application {
        private SQLiteDatabase db;
        private LocalSession session;

        @Override
        public void onCreate() {
           db = openOrCreateDatabase("name", MODE_PRIVATE, null);
           new RecordMigrator(db).migrate();
           session = new LocalSession(db);
        }

        public LocalSession getSession() {
            return session;
        }
    }

When your application starts the database will be setup and migrated once. After that you are able
to populate your database and query it. The following example shows how you can use the generated
classes:

.. code-block:: java

    LocalSession session = ((CustomApp)getApplication()).getSession();

    Picture picture = new Picture();
    picture.setImage(new byte[] { (byte)0xca, (byte)0xfe, (byte)0xba, (byte)0xbe });
    session.savePicture(picture); // save is insert or update at the same time.
    session.savePicture(picture); // this second call will invoke update
    long id = picture.getId();
    Picture picCopy = session.findPicture(id);
    session.destroyPicture(picCopy);
    picture = null;

