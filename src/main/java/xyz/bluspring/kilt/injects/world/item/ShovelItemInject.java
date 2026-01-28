// TRACKED HASH: 34022f33e65e37cd079c0272107db38813346ed5
package xyz.bluspring.kilt.injects.world.item;

import net.minecraft.tags.TagKey;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.common.ItemAbility;
import org.spongepowered.asm.mixin.Mixin;
import xyz.bluspring.kilt.helpers.mixin.CreateStatic;
import xyz.bluspring.kilt.injections.world.item.ShovelItemInjection;

@Mixin(ShovelItem.class)
public abstract class ShovelItemInject extends DiggerItem implements ShovelItemInjection {
    public ShovelItemInject(Tier tier, TagKey<Block> blocks, Properties properties) {
        super(tier, blocks, properties);
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