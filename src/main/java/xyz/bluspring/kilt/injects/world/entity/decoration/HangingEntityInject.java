package xyz.bluspring.kilt.injects.world.entity.decoration;

import com.llamalad7.mixinextras.injector.ModifyReceiver;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.HangingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import java.util.function.Predicate;
import java.util.stream.Stream;

@Mixin(HangingEntity.class)
public abstract class HangingEntityInject extends Entity {
    @Shadow protected Direction direction;

    public HangingEntityInject(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    @ModifyReceiver(method = "survives", at = @At(value = "INVOKE", target = "Ljava/util/stream/Stream;allMatch(Ljava/util/function/Predicate;)Z"))
    private <T> Stream<T> kilt$checkCanSupportCenter(Stream<T> instance, Predicate<? super T> predicate) {
        var level = this.level();
        var direction = this.direction;
        return instance.filter(pos -> !Block.canSupportCenter(level, (BlockPos) pos, direction));
    }
}
