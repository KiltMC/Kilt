package xyz.bluspring.kilt.injects.world.level.block;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EnchantingTableBlock;
import net.minecraft.world.level.block.state.BlockState;

@Mixin(EnchantingTableBlock.class)
public abstract class EnchantingTableBlockInject extends BaseEntityBlock {
    protected EnchantingTableBlockInject(Properties properties) {
        super(properties);
    }

    @Definition(id = "is", method = "Lnet/minecraft/world/level/block/state/BlockState;is(Lnet/minecraft/tags/TagKey;)Z")
    @Definition(id = "ENCHANTMENT_POWER_PROVIDER", field = "Lnet/minecraft/tags/BlockTags;ENCHANTMENT_POWER_PROVIDER:Lnet/minecraft/tags/TagKey;")
    @Expression("?.is(ENCHANTMENT_POWER_PROVIDER)")
    @WrapOperation(method = "isValidBookShelf", at = @At("MIXINEXTRAS:EXPRESSION"))
    private static boolean kilt$checkHasEnchantPowerBonus(BlockState instance, TagKey<Block> tagKey, Operation<Boolean> original, @Local(argsOnly = true) Level level, @Local(argsOnly = true, ordinal = 0) BlockPos pos, @Local(argsOnly = true, ordinal = 1) BlockPos offset) {
        return original.call(instance, tagKey) || instance.getEnchantPowerBonus(level, pos.offset(offset)) != 0;
    }
}
