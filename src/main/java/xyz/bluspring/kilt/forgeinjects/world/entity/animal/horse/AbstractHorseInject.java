package xyz.bluspring.kilt.forgeinjects.world.entity.animal.horse;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.wrapper.InvWrapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.bluspring.kilt.util.KiltHelper;

@Mixin(AbstractHorse.class)
public abstract class AbstractHorseInject extends Animal {
    @Shadow protected SimpleContainer inventory;

    protected AbstractHorseInject(EntityType<? extends Animal> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(method = "updateContainerEquipment", at = @At("TAIL"))
    private void kilt$initItemHandler(CallbackInfo ci) {
        var inventory = this.inventory;
        this.itemHandler = LazyOptional.of(() -> new InvWrapper(inventory));
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
        ForgeHooks.onLivingJump(this);
    }

    @Unique
    private LazyOptional<?> itemHandler = null;

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER && itemHandler != null && this.isAlive())
            return itemHandler.cast();

        return super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();

        if (itemHandler != null) {
            var oldHandler = this.itemHandler;
            this.itemHandler = null;
            oldHandler.invalidate();
        }
    }
}
