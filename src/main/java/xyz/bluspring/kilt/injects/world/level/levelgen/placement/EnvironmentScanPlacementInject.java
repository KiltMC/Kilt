package xyz.bluspring.kilt.injects.world.level.levelgen.placement;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.mojang.serialization.Codec;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.core.Direction;
import net.minecraft.world.level.levelgen.placement.EnvironmentScanPlacement;

@Mixin(EnvironmentScanPlacement.class)
public abstract class EnvironmentScanPlacementInject {
    @ModifyExpressionValue(method = "method_39626", at = @At(value = "FIELD", target = "Lnet/minecraft/core/Direction;VERTICAL_CODEC:Lcom/mojang/serialization/Codec;", opcode = Opcodes.GETSTATIC))
    private static Codec<Direction> kilt$allowAllDirections(Codec<Direction> original) {
        return Direction.CODEC;
    }
}
