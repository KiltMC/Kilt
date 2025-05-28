// TRACKED HASH: 65eab7af6923cfe40b811ec9f2b77f27d0284455
package xyz.bluspring.kilt.forgeinjects.network;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.extensions.IForgeFriendlyByteBuf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import xyz.bluspring.kilt.injections.network.FriendlyByteBufInjection;
import xyz.bluspring.kilt.util.KiltHelper;

@Mixin(FriendlyByteBuf.class)
public abstract class FriendlyByteBufInject implements IForgeFriendlyByteBuf, FriendlyByteBufInjection {
    @Shadow public abstract FriendlyByteBuf writeItem(ItemStack stack);

    // Kilt: Forge defaults to true, but we need to support Fabric mods that don't know this exists, so it defaults to false.
    @Unique private boolean kilt$isLimitedTag = false;

    @Override
    public FriendlyByteBuf writeItemStack(ItemStack stack, boolean limitedTag) {
        this.kilt$isLimitedTag = limitedTag;
        var buf = this.writeItem(stack);
        this.kilt$isLimitedTag = false;

        return buf;
    }

    @WrapOperation(method = "writeItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/Item;canBeDepleted()Z"))
    private boolean kilt$checkIsDamageable(Item instance, Operation<Boolean> original, @Local(argsOnly = true) ItemStack stack) {
        if (KiltHelper.INSTANCE.hasMethodOverride(instance.getClass(), Item.class, "isDamageable", ItemStack.class))
            return instance.isDamageable(stack);

        return original.call(instance);
    }

    @WrapOperation(method = "writeItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;getTag()Lnet/minecraft/nbt/CompoundTag;"))
    private CompoundTag kilt$checkIsLimitedTag(ItemStack instance, Operation<CompoundTag> original) {
        if (this.kilt$isLimitedTag)
            return instance.getShareTag();

        return original.call(instance);
    }

    @WrapOperation(method = "readItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;setTag(Lnet/minecraft/nbt/CompoundTag;)V"))
    private void kilt$readShareTagForStack(ItemStack instance, CompoundTag compoundTag, Operation<Void> original) {
        if (KiltHelper.INSTANCE.hasMethodOverride(instance.getItem().getClass(), Item.class, "readShareTag", ItemStack.class, CompoundTag.class))
            instance.readShareTag(compoundTag);
        else
            original.call(instance, compoundTag);
    }
}