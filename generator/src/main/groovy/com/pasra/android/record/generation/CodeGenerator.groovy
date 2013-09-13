package com.pasra.android.record.generation

/**
 * Created by rich on 9/10/13.
 */
class CodeGenerator {

    String formatType;
    int indentCount;
    int indent = 0;

    CodeGenerator(int indentCount = 4, String formatType = "java") {
        this.indentCount = indentCount
        this.formatType = formatType
    }

    StringBuilder builder = new StringBuilder();


    void indent(i = 1) {
        this.indent += i
    }

    void dedent(i = 1) {
        this.indent -= i
    }

    void wrap(head = "", Closure closure) {
        line("${head}{")
        indent()
            closure.run()
        dedent()
        line("}")
    }

    void write(str = "", newline = false, indentation = true) {
        if (indentation) {
            indent.times { i ->
                builder.append(" " * indentCount)
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
