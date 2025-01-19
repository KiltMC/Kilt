package xyz.bluspring.kilt.injections.entity;

import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.Pose;
import org.jetbrains.annotations.Nullable;

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
}
