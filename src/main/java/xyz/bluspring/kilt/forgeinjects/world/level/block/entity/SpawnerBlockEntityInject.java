package xyz.bluspring.kilt.forgeinjects.world.level.block.entity;

import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(SpawnerBlockEntity.class)
public abstract class SpawnerBlockEntityInject {
    @Mixin(targets = "net.minecraft.world.level.block.entity.SpawnerBlockEntity$1")
    public static abstract class BaseSpawnerInject {
        @Shadow @Final private SpawnerBlockEntity field_27219;

        @Nullable
        public BlockEntity getSpawnerBlockEntity() {
            return this.field_27219;
        }
    }
}
