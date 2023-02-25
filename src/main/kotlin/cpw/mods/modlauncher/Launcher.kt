package cpw.mods.modlauncher

// Reimplemented this because otherwise Aquaculture does not fucking function
class Launcher private constructor() {
    private val environment = Environment()

    fun environment(): Environment {
        return environment
    }

    companion object {
        @JvmField
        val INSTANCE: Launcher = Launcher()
    }
}