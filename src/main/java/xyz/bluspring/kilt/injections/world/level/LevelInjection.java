package xyz.bluspring.kilt.injections.world.level;

import io.github.fabricators_of_create.porting_lib.extensions.common.LevelExtensions;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.neoforged.neoforge.common.util.BlockSnapshot;
import xyz.bluspring.kilt.util.KiltHelper;

import javax.annotation.Nullable;
import java.util.ArrayList;

public interface LevelInjection {
    default ArrayList<BlockSnapshot> kilt$getCapturedBlockSnapshots() {
        throw new IllegalStateException();
    }

    default boolean kilt$getRestoringBlockSnapshots() {
        throw KiltHelper.createMixinException(LevelInjection.class, "kilt$getRestoringBlockSnapshots");
    }

    default boolean kilt$getCapturingBlockSnapshots() {
        throw KiltHelper.createMixinException(LevelInjection.class, "kilt$getCapturingBlockSnapshots");
    }

    default void kilt$setCapturingBlockSnapshots(boolean value) {
        throw KiltHelper.createMixinException(LevelInjection.class, "kilt$setCapturingBlockSnapshots");
    }

    default void kilt$setRestoringBlockSnapshots(boolean value) {
        throw KiltHelper.createMixinException(LevelInjection.class, "kilt$setRestoringBlockSnapshots");
    }

    default void setDayTimeFraction(float dayTimeFraction) {
        throw KiltHelper.createMixinException(LevelInjection.class, "setDayTimeFraction");
    }

    default float getDayTimeFraction() {
        throw KiltHelper.createMixinException(LevelInjection.class, "getDayTimeFraction");
    }

    default float getDayTimePerTick() {
        throw KiltHelper.createMixinException(LevelInjection.class, "getDayTimePerTick");
    }

    default void setDayTimePerTick(float dayTimePerTick) {
        throw KiltHelper.createMixinException(LevelInjection.class, "setDayTimePerTick");
    }

    default long advanceDaytime() {
        if (this.getDayTimePerTick() < 0) {
            return 1L;
        }

        float dayTimeStep = this.getDayTimeFraction() + this.getDayTimePerTick();
        long result = (long) dayTimeStep;
        this.setDayTimeFraction(dayTimeStep - result);

        return result;
    }

    default void markAndNotifyBlock(BlockPos pos, @Nullable LevelChunk levelchunk, BlockState oldState, BlockState newState, int flags, int p_46608_) {
        ((LevelExtensions) this).port_lib$markAndNotifyBlock(pos, levelchunk, oldState, newState, flags, p_46608_);
    }
}
