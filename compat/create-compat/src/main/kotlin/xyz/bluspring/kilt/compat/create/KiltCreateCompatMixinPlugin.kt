package xyz.bluspring.kilt.compat.create

import com.moulberry.mixinconstraints.MixinConstraints
import net.fabricmc.loader.api.FabricLoader
import org.objectweb.asm.tree.ClassNode
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin
import org.spongepowered.asm.mixin.extensibility.IMixinInfo
import xyz.bluspring.kilt.loader.KiltLoader

class KiltCreateCompatMixinPlugin : IMixinConfigPlugin {
    lateinit var mixinPackage: String

    override fun onLoad(mixinPackage: String) {
        this.mixinPackage = mixinPackage
    }

    override fun getRefMapperConfig(): String? {
        return null
    }

    override fun shouldApplyMixin(targetClassName: String, mixinClassName: String): Boolean {
        val packageName = mixinClassName.removePrefix("$mixinPackage.").replaceAfter(".", "").removeSuffix(".")

        if (packageName == "create_fabric") {
            return FabricLoader.getInstance().isModLoaded("create") && !KiltLoader.instance.hasMod("create") && MixinConstraints.shouldApplyMixin(mixinClassName)
        }

        if (packageName == "ponder") {
            return KiltLoader.instance.hasMod("ponder") && MixinConstraints.shouldApplyMixin(mixinClassName)
        }

        return MixinConstraints.shouldApplyMixin(mixinClassName)
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
