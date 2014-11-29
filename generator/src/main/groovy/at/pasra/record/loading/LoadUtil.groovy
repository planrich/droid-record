package at.pasra.record.loading

import at.pasra.record.database.Field

/**
 * Created by rich on 3/14/14.
 */
class LoadUtil {

    def static void string(def s) {
        if (!s instanceof String) {
            throw new IllegalArgumentException("expected String but got " + s.getClass())
        }
    }

    def static void primitive(def s) {
    }

    def static type(String s) {
        if (!Field.ALLOWED_TYPES.contains(s)) {
            throw new IllegalArgumentException("expected one of ${Field.ALLOWED_TYPES} but got ${s}")
        }
    }

    def static invoke(closure, structCtx, ctx = [:]) {
        closure.delegate = new LoadContext(ctx, structCtx)
        closure.resolveStrategy = Closure.DELEGATE_ONLY
        def result = closure()

        closure.delegate.__validate__()

        return result
    }

}


