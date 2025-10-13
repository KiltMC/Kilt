package xyz.bluspring.kilt.injects.world.entity.vehicle;

import com.mojang.datafixers.util.Either;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.vehicle.MinecartSpawner;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.common.extensions.IOwnedSpawner;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(MinecartSpawner.class)
public abstract class MinecartSpawnerInject {
    @Mixin(targets = "net.minecraft.world.entity.vehicle.MinecartSpawner$1")
    public static abstract class BaseSpawnerInject implements IOwnedSpawner {
        @Shadow @Final
        MinecartSpawner field_7747;

        @Override
        public Either<BlockEntity, Entity> getOwner() {
            return Either.right(field_7747);
        }
    }
}
