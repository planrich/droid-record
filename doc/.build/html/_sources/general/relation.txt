Relations
=========

..

@relations Relations
-#after migrations

%p
  Relations are all specified in one file. By default this is
  %span.migration-ref relations.json
  \.
  Relationship between tables is just meta information. At migration time this meta
  information is used to type check the primary and foreign keys on the relation.

%p
  In the following section the following migration is created before the relations
  are added.

%span.filename 123456_pre_migration.json
%pre
  %code{ data: { language: 'dsl' } }
    :preserve
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


relations|has_one Has One (1..1)

%p
  Given the following requirement: 'a user has one gallery' add this
  rule to your relationships:

%span.filename relations.json
%pre
  %code{ data: { language: 'dsl' } }
    :preserve
      ...
      user {
        has_one 'gallery'
      }


@relations|belongs_to Belongs to

%p
  Looking at the two sections above it might be useful that given a picture object you can
  retrieve it's gallery, or given a gallery you can lookup it's user. Add the following:

%span.filename relations.json
%pre
  %code{ data: { language: 'javascript' } }
    :preserve
      ...
      picture {
        belongs_to 'gallery'
      }
      gallery {
        belongs_to 'user'
      }


@relations|has_may Has Many (1..n)

%p Assuming that any gallery has many pictures (1..n):

%span.filename relations.json
%pre
  %code{ data: { language: 'dsl' } }
    :preserve
      ...
      gallery {
        has_many 'pictures'
      }

%p
  Note that the name in
  %span.migration-ref has_many
  array must be plural. This is more readable as you can simply read 'a gallery has many pictures'.
  If {?class:arname;DR} cannot infer the table from the given name in plural you can specifiy the exact
  table name by prepending a hash (#) infront of the name (e.g '#picture' instead of 'pictures').



@relations|has_and_belongs_to Has and belongs to

%p
  As an example consider the following requirement: "A user has many favourite pictures and a picture can be the favorite of many users".
  In a classic relational database this is called a n:m relation.

%span.filename relations.json
%pre
  %code{ data: { language: 'dsl' } }
    :preserve
      ...
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



@relations|has_and_belongs_to_options Has and belongs to options
-#after relations|has_and_belongs_to
%p
  You can specify the following options in the has_and_belongs_to object:
  %ul
    %li
      %strong many
      \- the target table name it belongs to. In the case of many it is a list of objects,
      in the other case it is just a single object
    %li
      %strong through
      \- the intermediate table name
    %li
      %strong foreign_key_has
      (optional). Specifies the name of the has foreign key. Use this if it differs
      from the naming convention
    %li
      %strong foreign_key_belongs_to
      (optional). Specifies the name of the foreign key it belongs to. Use it if its name
      differs from the naming convention


@relations|has_many_options Has many options
-#after relations|has_many
%p
  You can specify the following options:
  %ul
    %li
      %strong foreign_key
      \- the column name used to specify the foreign key. If this field is not specified
      the target table name (singular) is used and '_id' is appended.
      (e.g. target table name = 'stock_items', then the foreign key is 'stock_item_id')
    %li
      %strong many
      \- the pluralized table name or a hashed singular table name (e.g 'stock_items' or '#STOCK_ITEM').
      The hash should only be used when dealing with legacy databases.

