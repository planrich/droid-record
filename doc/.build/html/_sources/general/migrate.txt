Migration
=========


To handle changes in the database schema {?class:arname;DR} supports several commands in
a migration change array.
The items are evaluated one after another. That means
that modifications take place in the order you define it.

DR provides migrations to create, drop, rename tables and add, remove and rename columns
and a custom migration which gives you full control over the database.


Creation and deletion
---------------------

.. code-block:: groovy
    create_table {
      name 'picture'
      fields {
        title {
          type 'string'
          init 'empty'
        }
        likes 'integer'
        data 'blob'
        datetime 'date'
      }
    }
    drop_table {
      table 'picture'
    }

In the example above a 'pictures' sql table will be created when the migration is run on Android.
The singular name will be used to address this relation.

Each key value pair of fields
is a column of the table with a specified type. In simple cases you can just specify
one of the following types.

* string - java.lang.String
* blob - byte[]
* integer - java.lang.Integer
* long - java.lang.Long
* date - java.util.Date
* boolean - java.lang.Boolean
* double - java.lang.Double

title has a more complex type and it specifies the initial (init) value of this column to be "empty".
Note that this value is then set in the constructor of a record object.

The generated Picture.java file will have getters and setters of each column.
After the drop command has been run all data will be lost. Forever!

Changing names
--------------

Sometimes it can happen that a record receives the wrong name.
This mistake is easy to correct:

.. code-block:: groovy

    rename_table {
      table 'picture'
      to 'jpg_picture'
    }
Before running ``gradle migrate`` you should consider renaming the Picture class to JpgPicture in your IDE.
By doing so you won't have to correct the code afterwards manually.

Add/Remove a column No record is limited to the fields you specify while creating the table.
Adding, removing and renaming of fields is just that easy:

.. code-block:: groovy

  add_column {
    table 'picture'
    column 'description'
    type 'string'
  }
  remove_column {
    table 'picture'
    column 'datetime'
  }
  rename_column {
    table 'picture'
    column 'title'
    to 'name'
  }

Column `description` will be added as a new column to the table.
You can specify the same properties here which you can specify when creating a table.

`datetime` will be removed. Note that after this migration has been run the data is lost forever!

`title` gets a `name`. Note that data conversion cannot be made here!


Complex migrations
------------------

 When all the above would fail when migrating the database one can implement
 the following interface:

.. code-block:: java

     public interface DataMigrator {
         //This custom migrator can be used when the normal migrations cannot handle
         //the conversion. Do _NOT_ create, drop or alter tables. Droid Record
         //cannot track these changes and this will lead to undefined behaviour.

         //It is not advisable to use any generated class here. As this class
         //might disappear in the app development process.
         void migrate(SQLiteDatabase db, long currentVersion, long targetVersion);
     }

Then add a migration and specifiy your ``DataMigrator`` subclass:

.. code-block:: groovy

    migrate_data { class_name 'org.your.company.DataMigratorImpl' }

