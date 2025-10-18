package xyz.bluspring.kilt.forgeinjects.world.entity.vehicle;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.vehicle.MinecartSpawner;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import xyz.bluspring.kilt.injections.world.level.BaseSpawnerInjection;

@Mixin(MinecartSpawner.class)
public abstract class MinecartSpawnerInject {
    @Mixin(targets = "net.minecraft.world.entity.vehicle.MinecartSpawner$1")
    public static abstract class BaseSpawnerInject implements BaseSpawnerInjection {
        @Shadow @Final private MinecartSpawner field_7747;

        @Override
        public Entity getSpawnerEntity() {
            return field_7747;
        }
    }
}
