package xyz.bluspring.kilt.injects.world.entity.vehicle.minecart;

import com.mojang.datafixers.util.Either;
import net.neoforged.neoforge.common.extensions.IOwnedSpawner;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.vehicle.minecart.MinecartSpawner;
import net.minecraft.world.level.block.entity.BlockEntity;

@Mixin(MinecartSpawner.class)
public abstract class MinecartSpawnerInject {
    @Mixin(targets = "net.minecraft.world.entity.vehicle.minecart.MinecartSpawner$1")
    public static abstract class BaseSpawnerInject implements IOwnedSpawner {
        @Shadow @Final
        MinecartSpawner this$0;

        @Override
        public Either<BlockEntity, Entity> getOwner() {
            return Either.right(this$0);
        }
    }
}
