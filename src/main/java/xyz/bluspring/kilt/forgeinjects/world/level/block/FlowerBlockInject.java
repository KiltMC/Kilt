// TRACKED HASH: a51925153440e3294d883d8ecadb92e6cd8e76c4
package xyz.bluspring.kilt.forgeinjects.world.level.block;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.FlowerBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.registries.ForgeRegistries;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.bluspring.kilt.helpers.mixin.CreateInitializer;

import java.util.function.Supplier;

@Mixin(FlowerBlock.class)
public abstract class FlowerBlockInject extends BushBlock {
    @Mutable @Shadow @Final private MobEffect suspiciousStewEffect;
    @Mutable @Shadow @Final private int effectDuration;
    @Unique private Supplier<MobEffect> suspiciousStewEffectSupplier;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void kilt$addDelegateMobEffect(MobEffect suspiciousStewEffect, int effectDuration, BlockBehaviour.Properties properties, CallbackInfo ci) {
        var delegate = ForgeRegistries.MOB_EFFECTS.getDelegate(suspiciousStewEffect);
        if (delegate.isPresent()) {
            this.suspiciousStewEffectSupplier = delegate.orElseThrow();
        } else {
            this.suspiciousStewEffectSupplier = () -> suspiciousStewEffect;
        }
    }

    @CreateInitializer
    public FlowerBlockInject(Supplier<MobEffect> effectSupplier, int effectDuration, BlockBehaviour.Properties properties) {
        super(properties);
        this.suspiciousStewEffect = null;
        this.suspiciousStewEffectSupplier = effectSupplier;
        this.effectDuration = effectDuration;
    }

    @ModifyReturnValue(method = "getSuspiciousEffect", at = @At("RETURN"))
    private MobEffect kilt$useForgeDelegateIfPossible(MobEffect original) {
        if (original == null)
            return this.suspiciousStewEffectSupplier.get();

        return original;
    }

    @Inject(method = "getEffectDuration", at = @At("HEAD"), cancellable = true)
    private void kilt$useSecondsIfForge(CallbackInfoReturnable<Integer> cir) {
        if (this.suspiciousStewEffect == null && !this.suspiciousStewEffectSupplier.get().isInstantenous()) {
            cir.setReturnValue(this.effectDuration * 20);
        }
    }
}