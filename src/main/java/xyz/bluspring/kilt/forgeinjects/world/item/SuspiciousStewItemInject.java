package xyz.bluspring.kilt.forgeinjects.world.item;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SuspiciousStewItem;
import net.minecraftforge.common.ForgeHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SuspiciousStewItem.class)
public abstract class SuspiciousStewItemInject {
    @Inject(method = "saveMobEffect", at = @At(value = "INVOKE", target = "Lnet/minecraft/nbt/CompoundTag;putInt(Ljava/lang/String;I)V", shift = At.Shift.AFTER))
    private static void kilt$saveMobEffect(ItemStack bowlStack, MobEffect effect, int effectDuration, CallbackInfo ci, @Local(ordinal = 1) CompoundTag nbt) {
        ForgeHooks.saveMobEffect(nbt, "forge:effect_id", effect);
    }

    @ModifyExpressionValue(method = "listPotionEffects", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/effect/MobEffect;byId(I)Lnet/minecraft/world/effect/MobEffect;"))
    private static MobEffect kilt$tryLoadMobEffect(MobEffect original, @Local(ordinal = 1) CompoundTag nbt) {
        return ForgeHooks.loadMobEffect(nbt, "forge:effect_id", original);
    }
}
