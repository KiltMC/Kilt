// TRACKED HASH: b90940f2b56f4abadcf439bc0d7b45a206633f68
package xyz.bluspring.kilt.forgeinjects.client.color.item;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.color.item.ItemColor;
import net.minecraft.client.color.item.ItemColors;
import net.minecraft.core.Holder;
import net.minecraft.core.IdMapper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.registries.ForgeRegistries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.HashMap;
import java.util.Map;

@Mixin(ItemColors.class)
public class ItemColorsInject {
    @Inject(at = @At("RETURN"), method = "createDefault")
    private static void kilt$initForgeItemColors(BlockColors blockColors, CallbackInfoReturnable<ItemColors> cir) {
        ForgeHooksClient.onItemColorsInit(cir.getReturnValue(), blockColors);
    }

    @Unique
    private Map<Holder.Reference<Item>, ItemColor> kilt$itemColors;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void kilt$createForgeItemColorsWorkaround(CallbackInfo ci) {
        this.kilt$itemColors = new HashMap<>();
    }

    @WrapOperation(method = "getColor*", at = @At(value = "INVOKE", target = "Lnet/minecraft/core/IdMapper;byId(I)Ljava/lang/Object;"))
    private <T> T kilt$useForgeItemColorIfPossible(IdMapper<T> instance, int id, Operation<T> original, @Local(argsOnly = true) ItemStack stack) {
        var delegate = ForgeRegistries.ITEMS.getDelegate(stack.getItem());
        if (delegate.isPresent() && this.kilt$itemColors.containsKey(delegate.get())) {
            return (T) this.kilt$itemColors.get(delegate.get());
        }

        return original.call(instance, id);
    }

    @Inject(method = "register", at = @At(value = "INVOKE", target = "Lnet/minecraft/core/IdMapper;addMapping(Ljava/lang/Object;I)V"))
    private void kilt$registerItemToForgeColor(ItemColor itemColor, ItemLike[] items, CallbackInfo ci, @Local ItemLike item) {
        var delegate = ForgeRegistries.ITEMS.getDelegate(item.asItem());

        if (delegate.isPresent()) {
            this.kilt$itemColors.put(delegate.get(), itemColor);
        }
    }
}