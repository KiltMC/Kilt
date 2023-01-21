package xyz.bluspring.kilt.mixin;

import net.minecraft.data.DataGenerator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.nio.file.Path;
import java.util.Collection;

@Mixin(DataGenerator.class)
public interface DataGeneratorAccessor {
    @Accessor
    Collection<Path> getInputFolders();

    @Mutable
    @Accessor
    void setInputFolders(Collection<Path> inputFolders);
}
