package xyz.bluspring.kilt.compat.create

import com.bawnorton.mixinsquared.api.MixinCanceller
import net.fabricmc.loader.api.FabricLoader

class KiltCreateCompatMixinCanceller : MixinCanceller {
    override fun shouldCancel(targetClassNames: List<String>, mixinClassName: String): Boolean {
        // Avoid registering mixins that are already handled by Colorwheel Fabric
        if (mixinClassName.startsWith("dev.djefrey.colorwheel.neoforge.mixin")) {
            // TODO: we should make this actually better
            val sableFabric = FabricLoader.getInstance().getModContainer("colorwheel").orElseThrow()

            if (sableFabric.findPath(mixinClassName.replace(".", "/").replace("/colorwheel/neoforge/", "/colorwheel/fabric/") + ".class").isPresent) {
                return true
            }
        }

        return false
    }
}
