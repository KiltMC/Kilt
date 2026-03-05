package xyz.bluspring.kilt.mixin;

import net.minecraft.nbt.Tag;
import net.minecraft.world.level.storage.LevelStorageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.io.IOException;
import java.nio.file.Path;

@Mixin(LevelStorageSource.class)
public interface LevelStorageSourceAccessor {
    @Invoker
    static Tag callReadLightweightData(Path file) throws IOException {
        throw new UnsupportedOperationException();
    }
}
