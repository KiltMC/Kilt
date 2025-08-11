package xyz.bluspring.kilt.injects.world.entity.vehicle;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.vehicle.MinecartSpawner;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(MinecartSpawner.class)
public abstract class MinecartSpawnerInject {
    @Mixin(targets = "net.minecraft.world.entity.vehicle.MinecartSpawner$0")
    public static abstract class BaseSpawnerInject {
        @Shadow @Final private MinecartSpawner field_7747;

        @Override
        public Entity getSpawnerEntity() {
            return field_7747;
        }
    }
}
