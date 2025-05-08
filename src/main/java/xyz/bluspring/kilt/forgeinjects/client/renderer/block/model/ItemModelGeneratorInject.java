package xyz.bluspring.kilt.forgeinjects.client.renderer.block.model;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.block.model.ItemModelGenerator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import xyz.bluspring.kilt.injections.client.renderer.block.model.BlockModelInjection;

@Mixin(ItemModelGenerator.class)
public abstract class ItemModelGeneratorInject {
    @ModifyReturnValue(method = "generateBlockModel", at = @At("RETURN"))
    private BlockModel kilt$copyCustomBlockModelData(BlockModel original, @Local(argsOnly = true) BlockModel source) {
        ((BlockModelInjection) original).kilt$getCustomData().copyFrom(((BlockModelInjection) source).kilt$getCustomData());
        ((BlockModelInjection) original).kilt$getCustomData().setGui3d(false);

        return original;
    }
}
