package xyz.bluspring.kilt.injections.world.entity.player;

import java.util.Collection;

import org.jetbrains.annotations.Nullable;
import xyz.bluspring.kilt.util.KiltHelper;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public interface PlayerInjection extends io.github.fabricators_of_create.porting_lib.blocks.injects.PlayerInjection, io.github.fabricators_of_create.porting_lib.entity.injects.PlayerInjection {
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

    default void kilt$storeDugBlockPos(BlockPos pos) {
        throw KiltHelper.createMixinException(PlayerInjection.class, "kilt$storeDugBlockPos");
    }

    default boolean hasCorrectToolForDrops(BlockState state, Level level, BlockPos pos) {
        throw KiltHelper.createMixinException(PlayerInjection.class, "hasCorrectToolForDrops");
    }
}
