package xyz.bluspring.kilt.injections.world.level;

import net.minecraftforge.common.util.BlockSnapshot;

import java.util.ArrayList;

public interface LevelInjection {
    default ArrayList<BlockSnapshot> kilt$getCapturedBlockSnapshots() {
        throw new IllegalStateException();
    }
    default void kilt$setCaptureBlockSnapshots(boolean value) {
        throw new IllegalStateException();
    }
    default void kilt$setRestoringBlockSnapshots(boolean value) {
        throw new IllegalStateException();
    }
}
