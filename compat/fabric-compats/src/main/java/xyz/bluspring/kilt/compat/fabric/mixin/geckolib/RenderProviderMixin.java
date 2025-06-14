package xyz.bluspring.kilt.compat.fabric.mixin.geckolib;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.moulberry.mixinconstraints.annotations.IfModLoaded;
import net.minecraft.world.item.Item;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import software.bernie.geckolib.animatable.GeoItem;
import software.bernie.geckolib.animatable.client.RenderProvider;
import xyz.bluspring.kilt.compat.fabric.geckolib.ForgeRenderProvider;

import java.util.function.Supplier;

@IfModLoaded("geckolib")
@Pseudo
@Mixin(targets = "software/bernie/geckolib/animatable/client/RenderProvider")
public interface RenderProviderMixin {
    @WrapOperation(method = "of(Lnet/minecraft/world/item/Item;)Lsoftware/bernie/geckolib/animatable/client/RenderProvider;", at = @At(value = "INVOKE", target = "Lsoftware/bernie/geckolib/animatable/GeoItem;getRenderProvider()Ljava/util/function/Supplier;"))
    private static Supplier<RenderProvider> kilt$useWorkaroundRenderProvider(GeoItem instance, Operation<Supplier<RenderProvider>> original, @Local(argsOnly = true) Item item) {
        var extensions = IClientItemExtensions.of(item);

        if (extensions != IClientItemExtensions.DEFAULT) {
            return () -> ForgeRenderProvider.Companion.get(extensions);
        }

        return original.call(instance);
    }
}

