package xyz.bluspring.kilt.injections.client.render;

import com.mojang.math.Matrix4f;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.RecordItem;
import org.jetbrains.annotations.Nullable;

public interface LevelRendererInjection {
    Matrix4f getProjectionMatrix();
    void playStreamingMusic(@Nullable SoundEvent soundEvent, BlockPos pos, @Nullable RecordItem musicDiscItem);
}
