package at.pasra.record.generation

import at.pasra.record.DroidRecordPlugin

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

    void copyrightHeader() {
        if (formatType == "java") {
            line("""/* Copyright (c) 2013, Richard Plangger <rich@pasra.at> All rights reserved.
 *
 * Android Record version ${DroidRecordPlugin.VERSION} generated this file. For more
 * information see http://record.pasra.at/
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS
 * OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY
 * AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
 * THE POSSIBILITY OF SUCH DAMAGE.
 *
 * This copyright notice must not be modified or deleted.
 */""")
        }
    }

    void doNotModify() {
        if (formatType == "java") {
            line("// This file is generated. If you want to save you some time: !!!DO NOT MODIFY!!!")
        }
    }
}
