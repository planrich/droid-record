package at.pasra.record.loading

import org.gradle.api.logging.Logging

/**
 * Created by rich on 3/14/14.
 */
class LoadContext {

    def logger = Logging.getLogger(LoadContext.class)

    def structCtx
    def anyProp = false
    def ctx = null
    def invoked = []

    LoadContext(def c, def wl) {
        this.ctx = c
        this.structCtx = wl
        if (this.structCtx['*']) {
            this.anyProp = true;
        }
    }

    def methodMissing(String name, Object arg) {

        logger.info("method missing in load context. search for method ${name} with arg ${arg}")
        def structValue = structCtx[name]
        if (anyProp) {
            structValue = structCtx['*']
        }

        if (structValue == null) {
            throw new IllegalArgumentException("${name} is not allowed! one can use: ${structCtx}")
        } else {
            invoked << name

            def fileValue = arg[0]

            if (structValue instanceof List) {
                // this is a choice
                def theChoice = null
                structValue.each { choice ->
                    if (theChoice == null) {
                        if (choice instanceof Closure) {
                            try {
                                choice(fileValue)
                                theChoice = choice
                            } catch (Exception e) {
                            }
                        } else {
                            theChoice = choice;
                        }
                    }
                }

                if (theChoice == null) {
                    throw new IllegalArgumentException("failed to take choice!!!")
                }

                structValue = theChoice;
            }

            if (fileValue instanceof Closure) {
                // per default the context obj is a map where the data is stored in
                def obj = ctx[name] = [:]

                // if __fields__ is defined it must be a closure to create the context obj for the next invoke
                def prop = "__${anyProp ? '*' : name}__"
                if (structCtx[prop] != null) {
                    obj = ctx[name] = structCtx[prop](name)
                }
                LoadUtil.invoke(fileValue, structValue, obj)
            } else {

                def prop = "__${anyProp ? '*' : name}__"
                if (structCtx[prop] != null) {
                    def obj = ctx[name] = structCtx[prop]()
                    def assign_name = name
                    // * can have choices -> cannot determine the sub property if it is a simple type -> set it with that weird name...
                    if (structCtx["__*__default_value_property__"]) {
                        assign_name = structCtx["__*__default_value_property__"]
                    }
                    obj[assign_name] = fileValue
                    // * is the name itself! the sub object might needs it
                    if (structCtx["__*__key_property__"]) {
                        obj[structCtx["__*__key_property__"]] = name
                    }
                } else {
                    ctx[name] = fileValue
                }
            }
        }

        return ctx
    }

    def __validate__() {
        def req = structCtx['__required__']
        if (req) {
           req.each { String field ->
               if (!invoked.contains(field)) {
                   throw new IllegalArgumentException("Field ${field} is required but it is missing.")
               }
           }
        }
    }
}
