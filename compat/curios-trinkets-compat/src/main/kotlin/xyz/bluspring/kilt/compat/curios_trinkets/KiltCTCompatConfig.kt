package xyz.bluspring.kilt.compat.curios_trinkets

import net.neoforged.neoforge.common.ForgeConfigSpec

object KiltCTCompatConfig {
    val builder = ForgeConfigSpec.Builder()

    val guiMode = builder.comment("Which mod's GUI should be used?")
        .defineEnum("guiMode", GuiMode.TRINKETS)
}