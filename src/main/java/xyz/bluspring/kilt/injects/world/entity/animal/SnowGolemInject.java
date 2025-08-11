package xyz.bluspring.kilt.injects.world.entity.animal;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.AbstractGolem;
import net.minecraft.world.entity.animal.SnowGolem;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.IForgeShearable;
import net.neoforged.neoforge.event.EventHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(SnowGolem.class)
public abstract class SnowGolemInject extends AbstractGolem implements IForgeShearable {
    protected SnowGolemInject(EntityType<? extends AbstractGolem> entityType, Level level) {
        super(entityType, level);
    }

    @ModifyExpressionValue(method = "aiStep", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/GameRules;getBoolean(Lnet/minecraft/world/level/GameRules$Key;)Z"))
    private boolean kilt$checkMobGriefing(boolean original) {
        return original || EventHooks.getMobGriefingEvent(this.level(), this);
    }

    @ModifyExpressionValue(method = "aiStep", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;isAir()Z"))
    private boolean kilt$checkIsEmptyBlock(boolean original, @Local BlockPos pos) {
        return original || this.level().isEmptyBlock(pos);
    }

    // Kilt: Shearing handled by Porting Lib
}
