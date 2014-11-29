package at.pasra.record.loading

import at.pasra.record.database.BelongsTo
import at.pasra.record.database.HasAndBelongsTo
import at.pasra.record.database.HasMany
import at.pasra.record.database.HasOne
import at.pasra.record.database.Table
import at.pasra.record.generation.CodeGenerator
import at.pasra.record.util.Inflector

/**
 * Created by rich on 3/14/14.
 */
class RelationInvokeableContext {

    MigrationContext context;
    Table origin_table

    RelationInvokeableContext(MigrationContext ctx, Table o) {
        this.origin_table = o
        this.context = ctx
    }

    /*!
     *@relations|has_one Has One (1..1)
     *
     * %p
     *   Given the following requirement: 'a user has one gallery' add this
     *   rule to your relationships:
     *
     * %span.filename relations.json
     * %pre
     *   %code{ data: { language: 'dsl' } }
     *     :preserve
     *       ...
     *       user {
     *         has_one 'gallery'
     *       }
     */
    def has_one(Object o) {
        if (o instanceof Closure) {
            _has_one(o)
        } else {
            _has_one { ->
                table o.toString()
            }
        }
    }

    def _has_one(Closure c) {
        def structCtx = [
                'table': LoadUtil.&string,
                'primary_key': LoadUtil.&string,
                'foreign_key': LoadUtil.&string,
                '__required__': ['table']
        ]
        def r = LoadUtil.invoke(c, structCtx, new HasOne(origin_table))

        def target_name = Inflector.internalName(r.table)
        def target = context.getTable(target_name, "Has one relation expects table '${target_name}' to exist. But it does not!")
        r.target = target
        r.checkIntegrity();
        origin_table.relations << r
    }

    /*!
     * @relations|belongs_to Belongs to
     *
     * %p
     *   Looking at the two sections above it might be useful that given a picture object you can
     *   retrieve it's gallery, or given a gallery you can lookup it's user. Add the following:
     *
     * %span.filename relations.json
     * %pre
     *   %code{ data: { language: 'javascript' } }
     *     :preserve
     *       ...
     *       picture {
     *         belongs_to 'gallery'
     *       }
     *       gallery {
     *         belongs_to 'user'
     *       }
     */
    def belongs_to(Object o) {
        if (o instanceof Closure) {
            _belongs_to(o)
        } else {
            _belongs_to { ->
                to o.toString()
            }
        }
    }
    def _belongs_to(Closure c) {
        def structCtx = [
                'to': LoadUtil.&string,
                'primary_key': LoadUtil.&string,
                'foreign_key': LoadUtil.&string,
                '__required__': ['to']
        ]
        def r = LoadUtil.invoke(c, structCtx, new BelongsTo(origin_table))

        def target_name = Inflector.internalName(r.to)
        def target = context.getTable(target_name, "Belongs to relation expects table '${target_name}' to exist. But it does not!")
        r.target = target
        r.checkIntegrity();
        origin_table.relations << r
    }

    /*!
     * @relations|has_may Has Many (1..n)
     *
     * %p Assuming that any gallery has many pictures (1..n):
     *
     * %span.filename relations.json
     * %pre
     *   %code{ data: { language: 'dsl' } }
     *     :preserve
     *       ...
     *       gallery {
     *         has_many 'pictures'
     *       }
     *
     * %p
     *   Note that the name in
     *   %span.migration-ref has_many
     *   array must be plural. This is more readable as you can simply read 'a gallery has many pictures'.
     *   If {?class:arname;DR} cannot infer the table from the given name in plural you can specifiy the exact
     *   table name by prepending a hash (#) infront of the name (e.g '#picture' instead of 'pictures').
     *
     */
    def has_many(Object o) {
        if (o instanceof Closure) {
            _has_many(o)
        } else {
            _has_many { ->
                table o.toString()
            }
        }
    }
    def _has_many(Closure c) {

        def structCtx = [
                'table': LoadUtil.&string,
                'primary_key': LoadUtil.&string,
                'foreign_key': LoadUtil.&string,
                '__required__': ['table']
        ]
        def r = LoadUtil.invoke(c, structCtx, new HasMany(origin_table))

        def target_name = Inflector.internalName(Inflector.singularize(r.table))
        def target = context.getTable(target_name, "Has many relation expects table '${target_name}' to exist. But it does not! " +
                "If it does and the singularize function messed it up, use a " +
                "hash in front of the table name. e.g. 'has_many': '#singular_table_name'")
        r.target = target
        r.checkIntegrity();
        origin_table.relations << r
    }

    /*!
     * @relations|has_and_belongs_to Has and belongs to
     *
     * %p
     *   As an example consider the following requirement: "A user has many favourite pictures and a picture can be the favorite of many users".
     *   In a classic relational database this is called a n:m relation.
     *
     * %span.filename relations.json
     * %pre
     *   %code{ data: { language: 'dsl' } }
     *     :preserve
     *       ...
     *       user {
     *         has_and_belongs_to {
     *           many 'galleries'
     *           through 'user_picture'
     *         }
     *       }
     *       gallery {
     *         has_and_belongs_to {
     *           many: 'users'
     *           through: 'user_picture'
     *         }
     *       }
     *
     */
    def has_and_belongs_to(Closure c) {

        def structCtx = [
                'many': LoadUtil.&string,
                'through': LoadUtil.&string,
                '__required__': ['many','through']
        ]
        def r = LoadUtil.invoke(c, structCtx, new HasAndBelongsTo(origin_table))

        def many_table_name = Inflector.internalName(Inflector.singularize(r.many))
        def through_table_name = Inflector.internalName(Inflector.singularize(r.through))

        def many_table = context.getTable(many_table_name, "Expected table '${many_table_name}' to exist, but it does not!")
        def through_table = context.getTable(through_table_name, "Expected table '${through_table_name}' to exist, but it does not!")

        r.target = many_table
        r.through_table = through_table
        r.checkIntegrity()
        origin_table.relations << r

        def belongOrigin = new BelongsTo(through_table)
        belongOrigin.target = origin_table
        belongOrigin.checkIntegrity()
        through_table.relations << belongOrigin

        through_table.javaclass_codegen << { CodeGenerator cg ->
            def javaClassName = Inflector.javaClassName(through_table_name)
            def typeParam1 = Inflector.javaClassName(many_table_name)
            def nameParam1 = many_table_name
            def typeParam2 = origin_table.javaClassName
            def nameParam2 = origin_table.name
            /*cg.wrap("public static ${javaClassName} of(${typeParam1} ${nameParam1}, ${typeParam2} ${nameParam2})") {
                cg.line("${javaClassName} obj = new ${javaClassName}();")
                cg.line("obj.set${typeParam1}Id(${nameParam1}.getId());")
                cg.line("obj.set${typeParam2}Id(${nameParam2}.getId());")
                cg.line("return obj;")
            }*/
        }
    }

}
