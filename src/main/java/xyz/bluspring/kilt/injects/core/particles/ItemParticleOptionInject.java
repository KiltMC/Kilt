package xyz.bluspring.kilt.injects.core.particles;

import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemParticleOption.class)
public abstract class ItemParticleOptionInject {
    @Shadow @Final @Mutable private ItemStack itemStack;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void kilt$storeStackCopy(ParticleType type, ItemStack itemStack, CallbackInfo ci) {
        this.itemStack = this.itemStack.copy();
    }
}
