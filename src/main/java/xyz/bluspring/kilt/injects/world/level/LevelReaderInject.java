package xyz.bluspring.kilt.injects.world.level;

import io.github.fabricators_of_create.porting_lib.extensions.extensions.LevelReaderExtensions;
import net.minecraft.world.level.LevelReader;
import net.neoforged.neoforge.common.extensions.ILevelReaderExtension;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(LevelReader.class)
public interface LevelReaderInject extends LevelReaderExtensions, ILevelReaderExtension {
    // Kilt: implemented by Porting Lib
}
