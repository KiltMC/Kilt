package xyz.bluspring.kilt.forgeinjects.world.item;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MilkBucketItem;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fluids.capability.wrappers.FluidBucketWrapper;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.bluspring.kilt.injections.world.entity.LivingEntityInjection;

@Mixin(MilkBucketItem.class)
public abstract class MilkBucketItemInject extends Item {
    public MilkBucketItemInject(Properties properties) {
        super(properties);
    }

    @Inject(method = "finishUsingItem", at = @At("HEAD"))
    private void kilt$curePotionEffectsWithoutShrink(ItemStack stack, Level level, LivingEntity livingEntity, CallbackInfoReturnable<ItemStack> cir) {
        if (!level.isClientSide())
            ((LivingEntityInjection) livingEntity).curePotionEffects(stack);
    }

    @WrapOperation(method = "finishUsingItem", at = @At(value = "FIELD", target = "Lnet/minecraft/world/level/Level;isClientSide:Z"))
    private boolean kilt$cancelRemoveEffects(Level instance, Operation<Boolean> original) {
        return original.call(instance) && true;
    }

    @Override
    public @Nullable ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) {
        return new FluidBucketWrapper(stack);
    }
}
