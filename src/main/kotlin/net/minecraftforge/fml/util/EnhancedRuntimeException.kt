package net.minecraftforge.fml.util

import java.io.PrintStream
import java.io.PrintWriter
import java.io.StringWriter


abstract class EnhancedRuntimeException : RuntimeException {
    constructor() : super()
    constructor(message: String) : super(message)
    constructor(message: String, cause: Throwable) : super(message, cause)
    constructor(cause: Throwable) : super(cause)

    override val message: String?
        get() {
            val stack = Thread.currentThread().stackTrace
            if (stack.size > 2 && stack[2].className.startsWith("org.apache.logging.log4j.")) {
                val buf = StringWriter()
                val msg = super.message

                if (msg != null) buf.append(msg)
                buf.append('\n')

                this.printStackTrace(object : WrappedPrintStream() {
                    override fun println(line: String) {
                        buf.append(line).append('\n')
                    }
                })

                return buf.toString()
            }

            return super.message
        }

    open fun printStackTrace(s: PrintWriter) {
        printStackTrace(object : WrappedPrintStream() {
            override fun println(line: String) {
                s.println(line)
            }
        })
        super.printStackTrace(s)
    }

    open fun printStackTrace(s: PrintStream) {
        printStackTrace(object : WrappedPrintStream() {
            override fun println(line: String) {
                s.println(line)
            }
        })
        super.printStackTrace(s)
    }

    protected abstract fun printStackTrace(stream: WrappedPrintStream)

    abstract class WrappedPrintStream {
        abstract fun println(line: String)
    }
}