package xyz.bluspring.kilt.loader

object KiltFlags {
    // Mainly for debugging, so already-remapped Forge mods will be remapped again.
    @JvmField val FORCE_REMAPPING = "kilt.forceRemap".checkPropertyBoolean()

    // Mainly for debugging, used to test unobfuscated mods and ensure that Kilt is running as intended.
    @JvmField val DISABLE_REMAPPING = "kilt.noRemap".checkPropertyBoolean()

    // Mainly for debugging, to make sure all Forge mods remap correctly in production environments
    // without needing to actually launch a production environment.
    @JvmField val FORCE_PRODUCTION_REMAPPING = "kilt.forceProductionRemap".checkPropertyBoolean()

    // Disables coremods in all loaded Forge mods.
    @JvmField val DISABLE_COREMODS = !"kilt.disableCoreMods".checkPropertyBoolean()

    // Stores modified coremods into the .kilt/modifiedCoreMods directory
    @JvmField val STORE_MODIFIED_COREMODS = "kilt.storeModifiedCoreMods".checkPropertyBoolean()

    // Mainly for debugging, enables profiling if the DeltaTimeProfiler#dumpTree method is called.
    @JvmField val ENABLE_PROFILING = "kilt.enableProfiling".checkPropertyBoolean()

    // Mainly for debugging, enables logging access transformer info under the INFO level.
    // By default, AT info is logged under the DEBUG level, so it may still be found there.
    @JvmField val ENABLE_ACCESS_TRANSFORMER_DEBUG = "kilt.printATDebug".checkPropertyBoolean()

    // Some Forge mods call a System.exit if Kilt is present.
    // Kilt has a fixer that wraps System.exit to ensure users know why their game crashed, and additionally so it can
    // be tested in development. This flag allows the mod to be loaded anyway, completely overriding their code.
    @JvmField val DISABLE_FORGE_SYSTEM_EXIT = "kilt.disableSystemExit".checkPropertyBoolean()

    private fun String.checkPropertyBoolean(): Boolean {
        return System.getProperty(this)?.lowercase() == "true"
    }
}