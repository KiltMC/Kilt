package cpw.mods.modlauncher.log

object TransformingThrowablePatternConverter {
    @JvmStatic
    fun generateEnhancedStackTrace(throwable: Throwable): String {
        // i'm lazy
        return throwable.stackTraceToString()
    }
}