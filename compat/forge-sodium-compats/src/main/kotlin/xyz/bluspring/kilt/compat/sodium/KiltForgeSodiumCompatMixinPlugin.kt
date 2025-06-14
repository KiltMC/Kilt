package xyz.bluspring.kilt.compat.sodium

import com.moulberry.mixinconstraints.MixinConstraints
import org.objectweb.asm.tree.ClassNode
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin
import org.spongepowered.asm.mixin.extensibility.IMixinInfo
import xyz.bluspring.kilt.loader.KiltLoader

class KiltForgeSodiumCompatMixinPlugin : IMixinConfigPlugin {
    lateinit var mixinPackage: String

    override fun onLoad(mixinPackage: String) {
        this.mixinPackage = mixinPackage
    }

    override fun getRefMapperConfig(): String? {
        return null
    }

    override fun shouldApplyMixin(targetClassName: String?, mixinClassName: String): Boolean {
        return try {
            val modId = mixinClassName.removePrefix("$mixinPackage.").split(".")[0]
            KiltLoader.instance.hasMod(modId) && MixinConstraints.shouldApplyMixin(mixinClassName)
        } catch (_: Throwable) {
            MixinConstraints.shouldApplyMixin(mixinClassName)
        }
    }

    override fun acceptTargets(
        myTargets: Set<String?>?,
        otherTargets: Set<String?>?
    ) {
    }

    override fun getMixins(): List<String?>? {
        return null
    }

    override fun preApply(
        targetClassName: String?,
        targetClass: ClassNode?,
        mixinClassName: String?,
        mixinInfo: IMixinInfo?
    ) {
    }

    override fun postApply(
        targetClassName: String?,
        targetClass: ClassNode?,
        mixinClassName: String?,
        mixinInfo: IMixinInfo?
    ) {
    }
}