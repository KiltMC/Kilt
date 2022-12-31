package xyz.bluspring.kilt.loader

import net.minecraftforge.forgespi.locating.IModFile
import net.minecraftforge.forgespi.locating.IModProvider
import java.nio.file.Path
import java.util.function.Consumer

class KiltModProvider : IModProvider {
    override fun name(): String {
        return "Kilt Mod Provider"
    }

    override fun scanFile(modFile: IModFile, pathConsumer: Consumer<Path>) {
        TODO("Not yet implemented")
    }

    override fun initArguments(arguments: MutableMap<String, *>) {
        TODO("Not yet implemented")
    }

    override fun isValid(modFile: IModFile?): Boolean {
        TODO("Not yet implemented")
    }
}