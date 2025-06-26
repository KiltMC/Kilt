package xyz.bluspring.kilt.loader.remap.tiny

import net.fabricmc.tinyremapper.ClassInstance
import net.fabricmc.tinyremapper.api.*
import net.minecraftforge.fart.api.ClassProvider
import net.minecraftforge.srgutils.IMappingFile
import org.slf4j.event.Level
import xyz.bluspring.kilt.loader.remap.KiltEnhancedRemapper
import xyz.bluspring.kilt.loader.remap.KiltRemapper

class MixinTinyEnvironment(remapper: KiltEnhancedRemapper, val mapping: IMappingFile, private val classProvider: ClassProvider) : TrEnvironment {
    private val remapper = WrappedTinyRemapper(remapper, mapping)

    override fun getMrjVersion(): Int {
        return ClassInstance.MRJ_DEFAULT
    }

    override fun getRemapper(): TrRemapper {
        return remapper
    }

    override fun getLogger(): TrLogger? {
        return TrLogger { level, message -> KiltRemapper.logger.atLevel(Level.valueOf(level.name)).setMessage(message).log() }
    }

    override fun getClass(internalName: String?): TrClass? {
        val cls = classProvider.getClass(internalName).orElse(null) ?: return null
        return WrappedTinyClass(this, cls, classProvider)
    }

    override fun propagate(member: TrMember?, newName: String?) {
    }
}