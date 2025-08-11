package xyz.bluspring.kilt.injects.tags;

import com.bawnorton.mixinsquared.TargetHandler;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagManager;
import net.minecraftforge.registries.RegistryManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = TagManager.class, priority = 1050)
public abstract class TagManagerInject {
    // Kilt: Handled by Fabric API

    @TargetHandler(mixin = "net.fabricmc.fabric.mixin.registry.sync.TagManagerLoaderMixin", name = "onGetPath", prefix = "handler")
    @ModifyExpressionValue(method = "@MixinSquared:Handler", at = @At(value = "INVOKE", target = "Ljava/util/Set;contains(Ljava/lang/Object;)Z"))
    private static boolean kilt$checkIsForgeRegistry(boolean original, @Local(argsOnly = true)ResourceKey<? extends Registry<?>> registry) {
        // Kilt: Forge registries depend on this, so we want to handle it for Forge registries too.
        return original || RegistryManager.ACTIVE.getRegistry((ResourceKey) registry) != null;
    }
}
