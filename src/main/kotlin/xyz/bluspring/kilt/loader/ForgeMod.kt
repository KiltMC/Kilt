package xyz.bluspring.kilt.loader

//import net.minecraftforge.fml.common.Mod.EventBusSubscriber
import java.io.File

data class ForgeMod(
    val modInfo: ForgeModInfo,
    val events: MutableList<Any>,
    val modFile: File
)
