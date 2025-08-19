package xyz.bluspring.kilt.compat.fabric

import com.moulberry.mixinconstraints.MixinConstraints
import net.fabricmc.loader.api.FabricLoader
import org.objectweb.asm.tree.ClassNode
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin
import org.spongepowered.asm.mixin.extensibility.IMixinInfo
import xyz.bluspring.kilt.loader.KiltLoader

class KiltFabricCompatsMixinPlugin : IMixinConfigPlugin {
    lateinit var mixinPackage: String

    override fun onLoad(mixinPackage: String) {
        this.mixinPackage = mixinPackage
    }

    override fun getRefMapperConfig(): String? {
        return null
    }

    override fun shouldApplyMixin(targetClassName: String, mixinClassName: String): Boolean {
        val modId = mixinClassName.removePrefix("$mixinPackage.").replaceAfter(".", "").removeSuffix(".")

        if (modId == "sophisticatedcore" || modId == "creativecore") {
            return FabricLoader.getInstance().isModLoaded(modId) && !KiltLoader.instance.hasMod(modId) && MixinConstraints.shouldApplyMixin(targetClassName, mixinClassName)
        }

        if (modId == "accessories") {
            return FabricLoader.getInstance().isModLoaded("accessories")
                    && !KiltLoader.instance.hasMod("accessories")
                    && KiltLoader.instance.hasMod("cclayer")
        }

        return MixinConstraints.shouldApplyMixin(targetClassName, mixinClassName)
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