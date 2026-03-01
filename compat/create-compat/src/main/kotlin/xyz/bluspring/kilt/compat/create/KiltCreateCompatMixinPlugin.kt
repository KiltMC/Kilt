package xyz.bluspring.kilt.compat.create

import com.bawnorton.mixinsquared.ext.ExtensionRegistrar
import com.llamalad7.mixinextras.MixinExtrasBootstrap
import com.moulberry.mixinconstraints.MixinConstraints
import com.moulberry.mixinconstraints.mixin.MixinConstraintsBootstrap
import net.fabricmc.loader.api.FabricLoader
import org.objectweb.asm.tree.ClassNode
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin
import org.spongepowered.asm.mixin.extensibility.IMixinInfo
import xyz.bluspring.kilt.helpers.mixin.MixinExtensionHelper
import xyz.bluspring.kilt.loader.KiltLoader

class KiltCreateCompatMixinPlugin : IMixinConfigPlugin {
    lateinit var mixinPackage: String

    override fun onLoad(mixinPackage: String) {
        this.mixinPackage = mixinPackage

        // just in case
        MixinExtrasBootstrap.init()
        MixinConstraintsBootstrap.init(mixinPackage)

        ExtensionRegistrar.register(KiltCreateCompatMixinExtension())
    }

    override fun getRefMapperConfig(): String? {
        return null
    }

    override fun shouldApplyMixin(targetClassName: String, mixinClassName: String): Boolean {
        val packageName = mixinClassName.removePrefix("$mixinPackage.").replaceAfter(".", "").removeSuffix(".")

        if (packageName == "create_fabric") {
            return FabricLoader.getInstance().isModLoaded("create") && !KiltLoader.instance.hasMod("create") && MixinConstraints.shouldApplyMixin(mixinClassName)
        }

        if (packageName == "create_forge") {
            return KiltLoader.instance.hasMod("create") && MixinConstraints.shouldApplyMixin(mixinClassName)
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
        MixinExtensionHelper.preApply(targetClassName, targetClass, mixinClassName, mixinInfo)
    }

    override fun postApply(
        targetClassName: String?,
        targetClass: ClassNode,
        mixinClassName: String?,
        mixinInfo: IMixinInfo?
    ) {
        MixinExtensionHelper.postApply(targetClassName, targetClass, mixinClassName, mixinInfo)

        val packageName = mixinClassName?.removePrefix("$mixinPackage.")?.replaceAfter(".", "")?.removeSuffix(".")

        if (packageName == "registrate_fabric") {
            for (method in targetClass.methods) {
                if (method.desc.contains("L${KiltCreateCompatMixinExtension.TINY_TATER_TOKEN};")) {
                    KiltCreateCompatMixinExtension.modifyMethodDesc(method)
                }
            }
        }
    }
}