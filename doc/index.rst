.. Droid Record documentation master file, created by
   sphinx-quickstart on Sat May  2 15:36:21 2015.
   You can adapt this file completely to your liking, but it should at least
   contain the root `toctree` directive.

Welcome to Droid Record's documentation!
========================================

The documentation is currently not finished!

..
    If you used active record already you might find yourself
    very comfortable with this toolkit. Note that this is not an
    approach to clone active record as it would not make sense.
    After all ruby is a dynamic language moving a lot of it's functionality
    into the runtime. Java in this respect makes those things not that easy.
    Android is a mobile operating system and runs most likely on battery powered
    devices. Droid Record keeps that in mind and helps you to spend your time
    on the important stuff of your app, the business logic.


.. toctree::
   :maxdepth: 2

   general/setup
   general/migrate
   general/relation
   general/query

Changelog
---------

Version 0.1.4:
  * Default values implemented for boolean,integer,long,double and string
  * Migration (gradle command) now uses a number (starting from 1) instead of timestamp as prefix for migrations
Version 0.1.3:
  * Toolchain adaption
Version 0.1.2:
  * Split projects
Version 0.1.1:
  * Bugfix: Sorting, Table removal left null as reference
Version 0.1.0:
  * Syntax has changed to a more gradle like syntax
Version 0.0.8:
  * Added count to the query interface
Version 0.0.7:
  * Added double type
Version 0.0.6:
  * First Version
