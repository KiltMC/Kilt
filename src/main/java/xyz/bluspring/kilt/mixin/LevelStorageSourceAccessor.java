package xyz.bluspring.kilt.mixin;

import com.mojang.datafixers.DataFixer;
import net.minecraft.world.level.storage.LevelStorageSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.nio.file.Path;
import java.util.function.BiFunction;

@Mixin(LevelStorageSource.class)
public interface LevelStorageSourceAccessor {
    @Invoker
    <T> T callReadLevelData(LevelStorageSource.LevelDirectory levelDirectory, BiFunction<Path, DataFixer, T> levelDatReader);

    @Accessor
    Path getBaseDir();
}
