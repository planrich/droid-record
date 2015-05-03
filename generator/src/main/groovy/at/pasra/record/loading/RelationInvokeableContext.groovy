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
