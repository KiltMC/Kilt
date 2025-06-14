package xyz.bluspring.kilt.forgeinjects.world.level;

import io.github.fabricators_of_create.porting_lib.extensions.extensions.LevelReaderExtensions;
import net.minecraft.world.level.LevelReader;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(LevelReader.class)
public interface LevelReaderInject extends LevelReaderExtensions {
    // Kilt: implemented by Porting Lib
}
