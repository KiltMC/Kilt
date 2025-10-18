package xyz.bluspring.kilt.injections.world.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import xyz.bluspring.kilt.util.KiltHelper;

import java.util.Collection;

public interface PlayerInjection {
    String PERSISTED_NBT_TAG = "PlayerPersisted";

    default Collection<MutableComponent> getPrefixes() {
        throw new IllegalStateException();
    }

    default Collection<MutableComponent> getSuffixes()  {
        throw new IllegalStateException();
    }

    default void refreshDisplayName() {
        throw new IllegalStateException();
    }

    default void setForcedPose(@Nullable Pose pose) {
        throw new IllegalStateException();
    }

    default @Nullable Pose getForcedPose() {
        throw new IllegalStateException();
    }

    default float getDigSpeed(BlockState state, @Nullable BlockPos pos) {
        throw KiltHelper.createMixinException(PlayerInjection.class, "getDigSpeed");
    }

    default boolean hasCorrectToolForDrops(BlockState state, Level level, BlockPos pos) {
        throw KiltHelper.createMixinException(PlayerInjection.class, "hasCorrectToolForDrops");
    }
}
