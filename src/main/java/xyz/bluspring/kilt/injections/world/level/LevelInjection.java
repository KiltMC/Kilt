package xyz.bluspring.kilt.injections.world.level;

import net.neoforged.neoforge.common.util.BlockSnapshot;

import java.util.ArrayList;

public interface LevelInjection {
    default ArrayList<BlockSnapshot> kilt$getCapturedBlockSnapshots() {
        throw new IllegalStateException();
    }

    boolean kilt$getRestoringBlockSnapshots();
    boolean kilt$getCapturingBlockSnapshots();
    void kilt$setCapturingBlockSnapshots(boolean value);
    void kilt$setRestoringBlockSnapshots(boolean value);
}
