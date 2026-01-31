package xyz.bluspring.kilt.compat.create.mixin.registrate_fabric;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.moulberry.mixinconstraints.annotations.IfModLoaded;
import com.tterrag.registrate.AbstractRegistrate;
import com.tterrag.registrate.builders.AbstractBuilder;
import com.tterrag.registrate.builders.BuilderCallback;
import com.tterrag.registrate.builders.EntityBuilder;
import com.tterrag.registrate.util.nullness.NonNullConsumer;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import xyz.bluspring.kilt.Kilt;
import xyz.bluspring.kilt.compat.create.EntityBuilderAdapter;
import xyz.bluspring.kilt.compat.create.extensions.EntityBuilderExtension;

import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;

@IfModLoaded("registrate-fabric")
@Mixin(EntityBuilder.class)
public abstract class EntityBuilderMixin<T extends Entity, P> extends AbstractBuilder<EntityType<?>, EntityType<T>, P, EntityBuilder<T, P>> implements EntityBuilderExtension<T> {

    public EntityBuilderMixin(AbstractRegistrate<?> owner, P parent, String name, BuilderCallback callback, ResourceKey<Registry<EntityType<?>>> registryKey) {
        super(owner, parent, name, callback, registryKey);
    }

//    @Unique
//    private BiFunction<PlayMessages.SpawnEntity, Level, T> customClientFactory;

    @Unique
    private Predicate<EntityType<?>> velocityUpdateSupplier;

    @Unique
    private ToIntFunction<EntityType<?>> trackingRangeSupplier;

    @Unique
    private ToIntFunction<EntityType<?>> updateIntervalSupplier;

    @SuppressWarnings("unchecked")
    @ModifyArg(
            method = "properties",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/tterrag/registrate/util/nullness/NonNullConsumer;andThen(Lcom/tterrag/registrate/util/nullness/NonNullConsumer;)Lcom/tterrag/registrate/util/nullness/NonNullConsumer;"
            ),
            remap = false
    )
    public NonNullConsumer<Object> properties(NonNullConsumer<Object> after) {
        if (Kilt.Companion.getLoader().hasMod(getOwner().getModid())) {
            EntityBuilderExtension<T> builder = this;
            return fabricBuilder -> {
                if (fabricBuilder instanceof FabricEntityTypeBuilder<?>) {
                    after.accept(
                            new EntityBuilderAdapter<>((FabricEntityTypeBuilder<T>) fabricBuilder, builder)
                    );
                } else { // Should probably never happen, but just in case.
                    after.accept(fabricBuilder);
                }
            };
        }
        return after;
    }

//    @Override
//    public void kilt$setCustomClientFactory(@NotNull BiFunction<PlayMessages.SpawnEntity, Level, T> customClientFactory) {
//        this.customClientFactory = customClientFactory;
//    }

    @Override
    public void kilt$setVelocityUpdateSupplier(@NotNull Predicate<EntityType<?>> velocityUpdateSupplier) {
        this.velocityUpdateSupplier = velocityUpdateSupplier;
    }

    @Override
    public void kilt$setTrackingRangeSupplier(@NotNull ToIntFunction<EntityType<?>> trackingRangeSupplier) {
        this.trackingRangeSupplier = trackingRangeSupplier;
    }

    @Override
    public void kilt$setUpdateIntervalSupplier(@NotNull ToIntFunction<EntityType<?>> updateIntervalSupplier) {
        this.updateIntervalSupplier = updateIntervalSupplier;
    }

//    @SuppressWarnings("unchecked")
//    @ModifyReturnValue(
//            method = "createEntry()Lnet/minecraft/world/entity/EntityType;",
//            at = @At("RETURN")
//    )
//    protected EntityType<T> createEntry(EntityType<T> original) {
//        var entityType = (EntityTypeInjection<T>) original;
//        entityType.setCustomClientFactory(customClientFactory);
//        entityType.setTrackingRangeSupplier(trackingRangeSupplier);
//        entityType.setUpdateIntervalSupplier(updateIntervalSupplier);
//        entityType.setVelocityUpdateSupplier(velocityUpdateSupplier);
//        return original;
//    }

}
