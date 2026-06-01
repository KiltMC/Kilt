package cpw.mods.modlauncher.api

interface ITransformerActivity {
    companion object {
        const val COMPUTING_FRAMES_REASON = "computing_frames"
        const val CLASSLOADING_REASON = "classloading"
    }

    val context: Array<String>
    val type: Type
    val activityString: String

    enum class Type(val label: String) {
        PLUGIN("pl"), TRANSFORMER("xf"), REASON("re")
    }
}
