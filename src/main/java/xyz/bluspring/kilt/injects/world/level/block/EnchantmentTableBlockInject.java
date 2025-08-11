package xyz.bluspring.kilt.injects.world.level.block;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EnchantmentTableBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(EnchantmentTableBlock.class)
public abstract class EnchantmentTableBlockInject {
    @WrapOperation(method = "isValidBookShelf", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;is(Lnet/minecraft/tags/TagKey;)Z", ordinal = 0))
    private static boolean kilt$checkEnchantpowerBonus(BlockState instance, TagKey<Block> tagKey, Operation<Boolean> original, @Local(argsOnly = true) Level level, @Local(argsOnly = true, ordinal = 0) BlockPos tablePos, @Local(argsOnly = true, ordinal = 1) BlockPos offsetPos) {
        return original.call(instance, tagKey) || instance.getEnchantPowerBonus(level, tablePos.offset(offsetPos)) != 0;
    }
}
