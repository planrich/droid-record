package com.pasra.android.record.gen

/**
 * Created by rich on 9/10/13.
 */
class CodeGenerator {

    StringBuilder builder = new StringBuilder();

    int indent = 0;

    void indent(i = 1) {
        this.indent += i
    }

    void dedent(i = 1) {
        this.indent -= i
    }

    void wrap(head, Closure closure) {

        line("${head} {\n")
        indent()
            closure.run()
        dedent()
        line("}")
    }

    void write(str = "", newline = false, indentation = true) {
        if (indentation) {
            indent.times { i ->
                builder.append("    ")
            }
        }

        builder.append(str.toString())

        if (newline) {
            builder.append("\n")
        }
    }

    void line(str = "") {
        write(str, true)
    }

    @Override
    String toString() {
        return builder.toString();
    }

}
