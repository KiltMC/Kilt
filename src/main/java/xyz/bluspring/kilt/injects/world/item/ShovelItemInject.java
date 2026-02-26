// TRACKED HASH: 34022f33e65e37cd079c0272107db38813346ed5
package xyz.bluspring.kilt.injects.world.item;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.common.ItemAbility;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import xyz.bluspring.kilt.helpers.mixin.CreateStatic;
import xyz.bluspring.kilt.injections.world.item.ShovelItemInjection;
import xyz.bluspring.kilt.util.KiltHelper;

@Mixin(ShovelItem.class)
public abstract class ShovelItemInject extends DiggerItem implements ShovelItemInjection {
    public ShovelItemInject(Tier tier, TagKey<Block> blocks, Properties properties) {
        super(tier, blocks, properties);
    }

    @Definition(id = "blockState2", local = @Local(type = BlockState.class, ordinal = 1))
    @Expression("blockState2 != null")
    @ModifyVariable(method = "useOn", at = @At("MIXINEXTRAS:EXPRESSION"), ordinal = 1)
    private BlockState kilt$tryUseCustomToolModifiedState(BlockState original, @Local(ordinal = 0) BlockState state, @Local(argsOnly = true) UseOnContext context) {
        if (KiltHelper.INSTANCE.hasMethodOverride(state.getBlock().getClass(), Block.class, "getToolModifiedState", BlockState.class, UseOnContext.class, ItemAbility.class, boolean.class)) {
            return state.getToolModifiedState(context, ItemAbilities.SHOVEL_FLATTEN, false);
        }

        return original;
    }

    @Definition(id = "blockState3", local = @Local(type = BlockState.class, ordinal = 2))
    @Expression("blockState3 != null")
    @ModifyVariable(method = "useOn", at = @At("MIXINEXTRAS:EXPRESSION"), ordinal = 1)
    private BlockState kilt$tryUseCustomToolModifiedStateForDouse(BlockState original, @Local(ordinal = 0) BlockState state, @Local(argsOnly = true) UseOnContext context) {
        if (original == null && KiltHelper.INSTANCE.hasMethodOverride(state.getBlock().getClass(), Block.class, "getToolModifiedState", BlockState.class, UseOnContext.class, ItemAbility.class, boolean.class)) {
            return state.getToolModifiedState(context, ItemAbilities.SHOVEL_DOUSE, false);
        }

        return original;
    }

    @CreateStatic
    private static BlockState getShovelPathingState(BlockState originalState) {
        return ShovelItemInjection.getShovelPathingState(originalState);
    }

    @Override
    public boolean canPerformAction(ItemStack stack, ItemAbility toolAction) {
        return ItemAbilities.DEFAULT_SHOVEL_ACTIONS.contains(toolAction);
    }
}