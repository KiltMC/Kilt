package xyz.bluspring.kilt.injections.world.item;

import org.jetbrains.annotations.Nullable;
import xyz.bluspring.kilt.util.KiltHelper;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;

public interface BucketItemInjection {
    default boolean emptyContents(@Nullable Player player, Level level, BlockPos pos, @Nullable BlockHitResult result, @Nullable ItemStack container) {
        throw KiltHelper.createMixinException(BucketItemInjection.class, "emptyContents");
    }
}
