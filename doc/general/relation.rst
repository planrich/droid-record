Relations
=========

Relations are all specified in one file. By default this is ``relations.dr``.
Relationship between tables is just meta information. At migration time this meta
information is used to type check the primary and foreign keys on the relation.

In the following section the following migration is created before the relations
are added.

.. code-block:: groovy

  create_table {
    name 'picture'
    fields {
      name 'string'
      data 'blob'
      gallery_id 'long'
    }
  }
  create_table {
    name 'gallery'
    fields {
      name 'string'
      user_id 'long'
    }
  }
  create_table {
    name 'user'
    fields {
      name 'string'
      age 'int'
    }
  }
  create_table {
    name 'user_picture'
    fields {
      user_id 'long'
      picture_id 'long'
    }
  }


Has One (1<=>1)
--------------

Given the following requirement: 'a user has one gallery' add this
rule to your relationships:

.. code-block:: groovy

  user {
    has_one 'gallery'
  }


Belongs to
----------

Looking at the two sections above it might be useful that given a picture object you can
retrieve it's gallery, or given a gallery you can lookup it's user. Add the following:

.. code-block:: groovy

  picture {
    belongs_to 'gallery'
  }
  gallery {
    belongs_to 'user'
  }


Has Many (1<=>n)
---------------

A gallery has many pictures? It is just as easy as that:

.. code-block:: groovy

      gallery {
        has_many 'pictures'
      }

Note that the name in ``has_many`` array must be plural.
This is more readable as you can simply read 'a gallery has many pictures'.
If DR cannot infer the table from the given name in plural you can specifiy the exact
table name by prepending a hash (#) infront of the name (e.g '#picture' instead of 'pictures').

Options
-------
* **foreign_key**: The column name used to specify the foreign key. If this field is not specified
  the target table name (singular) is used and '_id' is appended. (e.g. target table name = 'stock_items', then the foreign key is 'stock_item_id')
* **many**: the pluralized table name or a hashed singular table name (e.g 'stock_items' or '#STOCK_ITEM'). The hash should only be used when dealing with legacy databases.

Has and belongs to
------------------

As an example consider the following requirement: "A user has many favourite pictures and a picture can be the favorite of many users".
In a classic relational database this is called a n:m relation.

.. code-block:: groovy

  user {
    has_and_belongs_to {
      many 'galleries'
      through 'user_picture'
    }
  }
  gallery {
    has_and_belongs_to {
      many: 'users'
      through: 'user_picture'
    }
  }



Options
--------------------------

You can specify the following options in the has_and_belongs_to object:
* **many**: the target table name it belongs to. In the case of many it is a list of objects, in the other case it is just a single object
* **through**: the intermediate table name
* **foreign_key_has**: (optional) Specifies the name of the has foreign key. Use this if it differs from the naming convention
* **foreign_key_belongs_to**: (optional) Specifies the name of the foreign key it belongs to. Use it if its name differs from the naming convention

