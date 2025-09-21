package xyz.bluspring.kilt.injects.world.level.chunk.storage;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import net.minecraft.world.level.chunk.storage.SectionStorage;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import xyz.bluspring.kilt.injections.world.level.chunk.storage.SectionStorageInjection;

import java.util.Optional;

@Mixin(SectionStorage.class)
public abstract class SectionStorageInject<R> implements SectionStorageInjection {
    @Shadow @Final private Long2ObjectMap<Optional<R>> storage;

    @Override
    public void remove(long sectionPosAsLong) {
        this.storage.remove(sectionPosAsLong);
    }
}
