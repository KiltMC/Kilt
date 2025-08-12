package net.minecraftforge.fml.util

class LoaderException : EnhancedRuntimeException {
    /**
     *
     */
    private val serialVersionUID: Long = -5675297950958861378L

    constructor() : super()

    constructor(wrapped: Throwable) : super(wrapped)

    constructor(message: String) : super(message)

    constructor(message: String, cause: Throwable) : super(message, cause)

    override fun printStackTrace(stream: WrappedPrintStream) {
    }
}