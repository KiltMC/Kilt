// TRACKED HASH: c90ad9c5c8bd04fe0240bdfe4249e3f318e2cd46
package xyz.bluspring.kilt.forgeinjects.world.effect;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.extensions.IForgeMobEffectInstance;
import net.minecraftforge.registries.ForgeRegistries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.bluspring.kilt.injections.world.effect.MobEffectInstanceInjection;

import java.util.ArrayList;
import java.util.List;

@Mixin(value = MobEffectInstance.class, priority = 1010)
public abstract class MobEffectInstanceInject implements IForgeMobEffectInstance, MobEffectInstanceInjection {
    @SuppressWarnings("MixinAnnotationTarget") @Shadow List<ItemStack> curativeItems;

    @Shadow public abstract MobEffect getEffect();

    @Inject(method = "<init>(Lnet/minecraft/world/effect/MobEffectInstance;)V", at = @At("TAIL"))
    private void kilt$initCurativeItems(MobEffectInstance other, CallbackInfo ci) {
        this.curativeItems = ((MobEffectInstanceInjection) other).kilt$getDirectCurativeItems() == null ? null : new ArrayList<>(((MobEffectInstanceInjection) other).kilt$getDirectCurativeItems());
    }

    @Override
    public List<ItemStack> kilt$getDirectCurativeItems() {
        return this.curativeItems;
    }

    @ModifyReturnValue(method = "getEffect", at = @At("RETURN"))
    private MobEffect kilt$getForgeDelegateEffect(MobEffect original) {
        var delegate = ForgeRegistries.MOB_EFFECTS.getDelegate(original);
        if (delegate.isPresent()) {
            return delegate.orElseThrow().value();
        }

        return original;
    }

    @Inject(method = "save", at = @At(value = "INVOKE", target = "Lnet/minecraft/nbt/CompoundTag;putInt(Ljava/lang/String;I)V", shift = At.Shift.AFTER))
    private void kilt$saveForgeMobEffect(CompoundTag nbt, CallbackInfoReturnable<CompoundTag> cir) {
        ForgeHooks.saveMobEffect(nbt, "forge:id", this.getEffect());
    }

    @Inject(method = "writeDetailsTo", at = @At(value = "INVOKE", target = "Ljava/util/Optional;ifPresent(Ljava/util/function/Consumer;)V"))
    private void kilt$writeCurativeItems(CompoundTag nbt, CallbackInfo ci) {
        this.writeCurativeItems(nbt);
    }

    @ModifyExpressionValue(method = "load", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/effect/MobEffect;byId(I)Lnet/minecraft/world/effect/MobEffect;"))
    private static MobEffect kilt$loadForgeMobEffect(MobEffect original, @Local(argsOnly = true) CompoundTag nbt) {
        return ForgeHooks.loadMobEffect(nbt, "forge:id", original);
    }

    @ModifyReturnValue(method = "loadSpecifiedEffect", at = @At("RETURN"))
    private static MobEffectInstance kilt$readCurativeItemsForEffect(MobEffectInstance original, @Local(argsOnly = true) CompoundTag nbt) {
        return readCurativeItems(original, nbt);
    }

    // Kilt: implemented by Porting Lib
    /*@Shadow public abstract MobEffect getEffect();
    @Unique private List<ItemStack> curativeItems;

    @Override
    public List<ItemStack> getCurativeItems() {
        if (this.curativeItems == null)
            this.curativeItems = this.getEffect().getCurativeItems();

        return curativeItems;
    }

    @Override
    public void setCurativeItems(List<ItemStack> curativeItems) {
        this.curativeItems = curativeItems;
    }*/

    private static MobEffectInstance readCurativeItems(MobEffectInstance effect, CompoundTag nbt) {
        if (nbt.contains("CurativeItems", Tag.TAG_LIST)) {
            var items = new ArrayList<ItemStack>();
            var list = nbt.getList("CurativeItems", Tag.TAG_COMPOUND);
            for (int i = 0; i < list.size(); i++) {
                items.add(ItemStack.of(list.getCompound(i)));
            }

            effect.setCurativeItems(items);
        }

        return effect;
    }
}