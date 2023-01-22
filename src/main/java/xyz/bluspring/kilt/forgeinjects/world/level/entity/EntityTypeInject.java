package xyz.bluspring.kilt.forgeinjects.world.level.entity;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.PlayMessages;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import xyz.bluspring.kilt.injections.entity.EntityTypeInjection;

import java.util.function.BiFunction;

@Mixin(EntityType.class)
public abstract class EntityTypeInject<T extends Entity> implements EntityTypeInjection<T> {
    @SuppressWarnings("MixinAnnotationTarget") // this is supposed to exist, but I guess mixin doesn't think so?
    @Shadow @Nullable public abstract T create(Level level);

    private BiFunction<PlayMessages.SpawnEntity, Level, T> customClientFactory;

    @Override
    public T customClientSpawn(PlayMessages.SpawnEntity packet, Level world) {
        if (customClientFactory == null)
            return this.create(world);

        return customClientFactory.apply(packet, world);
    }
}
