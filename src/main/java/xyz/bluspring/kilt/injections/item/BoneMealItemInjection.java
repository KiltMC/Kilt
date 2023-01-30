package xyz.bluspring.kilt.injections.item;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BoneMealItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.ForgeEventFactory;

import java.util.concurrent.atomic.AtomicBoolean;

public interface BoneMealItemInjection {
    AtomicBoolean kiltFired = new AtomicBoolean(false);

    static boolean applyBonemeal(ItemStack stack, Level level, BlockPos pos, Player player) {
        var blockState = level.getBlockState(pos);
        var hook = ForgeEventFactory.onApplyBonemeal(player, level, pos, blockState, stack);

        if (hook != 0)
            return hook > 0;

        kiltFired.set(true);
        return BoneMealItem.growCrop(stack, level, pos);
    }
}
