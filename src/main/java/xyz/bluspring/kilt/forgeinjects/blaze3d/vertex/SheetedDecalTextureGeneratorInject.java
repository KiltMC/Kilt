// TRACKED HASH: 3e3b2924e527b9eb6d570e4d7c6646c26ab80722
package xyz.bluspring.kilt.forgeinjects.blaze3d.vertex;

import com.mojang.blaze3d.vertex.SheetedDecalTextureGenerator;
import net.minecraft.core.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import xyz.bluspring.kilt.injections.core.DirectionInjection;

@Mixin(SheetedDecalTextureGenerator.class)
public class SheetedDecalTextureGeneratorInject {
    @Redirect(method = "endVertex", at = @At(value = "INVOKE", target = "Lnet/minecraft/core/Direction;getNearest(FFF)Lnet/minecraft/core/Direction;"))
    private Direction kilt$useStableNearest(float x, float y, float z) {
        return DirectionInjection.getNearestStable(x, y, z);
    }
}