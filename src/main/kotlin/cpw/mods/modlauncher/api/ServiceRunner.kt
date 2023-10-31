package cpw.mods.modlauncher.api

fun interface ServiceRunner {
    @Throws(Throwable::class)
    fun run()

    companion object {
        @JvmField
        val NOOP = ServiceRunner {}
    }
}