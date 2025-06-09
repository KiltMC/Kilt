package xyz.bluspring.kilt.forgeinjects.tags;

import net.minecraft.tags.TagManager;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value = TagManager.class, priority = 1050)
public abstract class TagManagerInject {
    /*@TargetHandler(mixin = "net.fabricmc.fabric.mixin.registry.sync.TagManagerLoaderMixin", name = "onGetPath", prefix = "handler")
    @ModifyExpressionValue(method = "@MixinSquared:Handler", at = @At(value = "INVOKE", target = "Ljava/util/Set;contains(Ljava/lang/Object;)Z"))
    private static boolean kilt$checkIsForgeRegistry(boolean original, @Local(argsOnly = true) ResourceKey<? extends Registry<?>> registry) {
        // Kilt: Forge registries depend on this, so we want to handle it for Forge registries too.
        return original || RegistryManager.ACTIVE.getRegistry((ResourceKey) registry) != null;
    }*/
}
