// TRACKED HASH: 13883d3a815e6b78fb2ecf76ce46710d2684597f
package xyz.bluspring.kilt.injects.world.level.block;

import java.util.Optional;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.bluspring.kilt.helpers.mixin.CreateInitializer;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.WoodType;

@Mixin(FenceGateBlock.class)
public abstract class FenceGateBlockInject extends HorizontalDirectionalBlock {
    public FenceGateBlockInject(WoodType type, Properties properties) {
        super(properties);
    }

    @Unique public SoundEvent openSound, closeSound;
    @Unique private boolean kilt$isCustomSounds;

    @CreateInitializer
    public FenceGateBlockInject(BlockBehaviour.Properties properties, SoundEvent openSound, SoundEvent closeSound) {
        this(Optional.empty(), properties, Optional.of(openSound), Optional.of(closeSound));
    }

    @CreateInitializer
    public FenceGateBlockInject(Optional<WoodType> woodType, BlockBehaviour.Properties properties, Optional<SoundEvent> openSound, Optional<SoundEvent> closeSound) {
        this(woodType.orElse(
            WoodType.OAK // Kilt: Use oak as a default
        ), properties);
        this.openSound = openSound.orElseThrow();
        this.closeSound = closeSound.orElseThrow();
        this.kilt$isCustomSounds = true;
    }

    @Inject(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/FenceGateBlock;registerDefaultState(Lnet/minecraft/world/level/block/state/BlockState;)V"))
    private void kilt$useCustomSounds(WoodType type, Properties properties, CallbackInfo ci) {
        if (type != null) {
            this.openSound = type.fenceGateOpen();
            this.closeSound = type.fenceGateClose();
        }
    }

    @WrapOperation(method = {"useWithoutItem", "onExplosionHit", "neighborChanged"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/properties/WoodType;fenceGateOpen()Lnet/minecraft/sounds/SoundEvent;"))
    private SoundEvent kilt$tryUseCustomOpenSound(WoodType instance, Operation<SoundEvent> original) {
        if (this.kilt$isCustomSounds) {
            return this.openSound;
        }

        return original.call(instance);
    }

    @WrapOperation(method = {"useWithoutItem", "onExplosionHit", "neighborChanged"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/properties/WoodType;fenceGateClose()Lnet/minecraft/sounds/SoundEvent;"))
    private SoundEvent kilt$tryUseCustomCloseSound(WoodType instance, Operation<SoundEvent> original) {
        if (this.kilt$isCustomSounds) {
            return this.closeSound;
        }

        return original.call(instance);
    }
}
