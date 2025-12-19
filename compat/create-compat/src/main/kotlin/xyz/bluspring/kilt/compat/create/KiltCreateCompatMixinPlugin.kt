package xyz.bluspring.kilt.compat.create

import com.moulberry.mixinconstraints.MixinConstraints
import net.fabricmc.loader.api.FabricLoader
import org.objectweb.asm.tree.ClassNode
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin
import org.spongepowered.asm.mixin.extensibility.IMixinInfo
import xyz.bluspring.kilt.loader.KiltLoader
import xyz.bluspring.kilt.loader.remap.KiltRemapper

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
        val tinyTaterToken = "xyz/bluspring/kilt/compat/create/TinyTater"
        val fluidTypeFactoryInterface = "com/tterrag/registrate/builders/FluidBuilder\$FluidTypeFactory"

        if(mixinClassName?.contains("AbstractRegistrateMixin") == true) {
            val method = targetClass?.methods?.firstOrNull { methodNode ->
                methodNode.desc == KiltRemapper.remapDescriptor("(Ljava/lang/String;Lnet/minecraft/class_2960;Lnet/minecraft/class_2960;Lxyz/bluspring/kilt/compat/create/TinyTater;Lcom/tterrag/registrate/util/nullness/NonNullFunction;)Lcom/tterrag/registrate/builders/FluidBuilder;")
            } ?: return

            method.desc = method.desc.replace("L$tinyTaterToken;", "L$fluidTypeFactoryInterface;")
        }

//                for (method in targetClass.methods) {
//                    method.desc = method.desc.replace("L$tinyTaterToken;", "L$fluidTypeFactoryInterface;")
//                }
    }
}