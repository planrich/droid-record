package at.pasra.record.loading

import at.pasra.record.database.Table
import at.pasra.record.util.Inflector

import javax.el.MethodNotFoundException;

/**
 * Created by rich on 3/14/14.
 */

/*!
 * @relations Relations
 * -#after migrations
 *
 * %p
 *   Relations are all specified in one file. By default this is
 *   %span.migration-ref relations.json
 *   \.
 *   Relationship between tables is just meta information. At migration time this meta
 *   information is used to type check the primary and foreign keys on the relation.
 *
 * %p
 *   In the following section the following migration is created before the relations
 *   are added.
 *
 * %span.filename 123456_pre_migration.json
 * %pre
 *   %code{ data: { language: 'dsl' } }
 *     :preserve
 *       create_table {
 *         name 'picture'
 *         fields {
 *           name 'string'
 *           data 'blob'
 *           gallery_id 'long'
 *         }
 *       }
 *       create_table {
 *         name 'gallery'
 *         fields {
 *           name 'string'
 *           user_id 'long'
 *         }
 *       }
 *       create_table {
 *         name 'user'
 *         fields {
 *           name 'string'
 *           age 'int'
 *         }
 *       }
 *       create_table {
 *         name 'user_picture'
 *         fields {
 *           user_id 'long'
 *           picture_id 'long'
 *         }
 *       }
 */
class RelationsRootContext {

    MigrationContext context;
    RelationsRootContext(MigrationContext ctx) {
        this.context = ctx;
    }

    def methodMissing(String name, args) {
        Table origin = context.getTable(Inflector.internalName(name), "Relation expects table '${name}' to exist. But it does not!")
        if (args.size() == 1 && args[0] instanceof Closure) {
            Closure c = args[0]
            def delegate = new RelationInvokeableContext(context, origin)
            c.delegate = delegate
            c.resolveStrategy = Closure.DELEGATE_ONLY
            c()
        } else {
            throw new MethodNotFoundException("You must provide a closure to a relation!")
        }
    }
}
