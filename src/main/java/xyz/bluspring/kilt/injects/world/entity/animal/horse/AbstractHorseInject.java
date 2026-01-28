package xyz.bluspring.kilt.injects.world.entity.animal.horse;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.CommonHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.bluspring.kilt.injections.world.entity.animal.horse.AbstractHorseInjection;
import xyz.bluspring.kilt.util.KiltHelper;

@Mixin(AbstractHorse.class)
public abstract class AbstractHorseInject extends Animal implements AbstractHorseInjection {
    @Shadow protected SimpleContainer inventory;

    protected AbstractHorseInject(EntityType<? extends Animal> entityType, Level level) {
        super(entityType, level);
    }

    @WrapOperation(method = "playStepSound", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;getSoundType()Lnet/minecraft/world/level/block/SoundType;"))
    private SoundType kilt$tryUseForgeSoundType(BlockState instance, Operation<SoundType> original, @Local(argsOnly = true) BlockPos pos) {
        if (KiltHelper.INSTANCE.hasMethodOverride(instance.getBlock().getClass(), Block.class, "getSoundType", BlockState.class, LevelReader.class, BlockPos.class, Entity.class)) {
            return instance.getSoundType(this.level(), pos, this);
        }

        return original.call(instance);
    }

    @Definition(id = "travelVector", local = @Local(type = Vec3.class, ordinal = 0, argsOnly = true))
    @Definition(id = "z", field = "Lnet/minecraft/world/phys/Vec3;z:D")
    @Expression("travelVector.z > 0.0")
    @Inject(method = "executeRidersJump", at = @At("MIXINEXTRAS:EXPRESSION"))
    private void kilt$callLivingJump(float playerJumpPendingScale, Vec3 travelVector, CallbackInfo ci) {
        CommonHooks.onLivingJump(this);
    }

    public Container getInventory() {
        return this.inventory;
    }
}
