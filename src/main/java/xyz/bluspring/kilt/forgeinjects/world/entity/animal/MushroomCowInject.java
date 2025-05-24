package xyz.bluspring.kilt.forgeinjects.world.entity.animal;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.animal.MushroomCow;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.IForgeShearable;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MushroomCow.class)
public abstract class MushroomCowInject implements IForgeShearable {
    // Kilt: Shearing handled by Porting Lib

    @Shadow private @Nullable MobEffect effect;

    @Inject(method = "addAdditionalSaveData", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/effect/MobEffect;getId(Lnet/minecraft/world/effect/MobEffect;)I"))
    private void kilt$tryStoreMobEffect(CompoundTag tag, CallbackInfo ci) {
        ForgeHooks.saveMobEffect(tag, "forge:effect_id", this.effect);
    }

    @ModifyExpressionValue(method = "readAdditionalSaveData", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/effect/MobEffect;byId(I)Lnet/minecraft/world/effect/MobEffect;"))
    private MobEffect kilt$tryLoadForgeEffect(MobEffect original, @Local(argsOnly = true) CompoundTag tag) {
        return ForgeHooks.loadMobEffect(tag, "forge:effect_id", original);
    }
}
