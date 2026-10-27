package xyz.bluspring.kilt.injects.world.entity.decoration;

import java.util.function.Predicate;
import java.util.stream.Stream;

import com.llamalad7.mixinextras.injector.ModifyReceiver;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.HangingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

@Mixin(HangingEntity.class)
public abstract class HangingEntityInject extends Entity {
    public HangingEntityInject(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    @ModifyReceiver(method = "survives", at = @At(value = "INVOKE", target = "Ljava/util/stream/Stream;allMatch(Ljava/util/function/Predicate;)Z"))
    private <T> Stream<T> kilt$checkCanSupportCenter(Stream<T> instance, Predicate<? super T> predicate) {
        var level = this.level();
        var direction = this.getDirection();
        return instance.filter(pos -> !Block.canSupportCenter(level, (BlockPos) pos, direction));
    }
}
