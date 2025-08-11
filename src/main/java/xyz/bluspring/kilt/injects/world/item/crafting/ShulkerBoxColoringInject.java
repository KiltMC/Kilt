package xyz.bluspring.kilt.injects.world.item.crafting;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.ShulkerBoxColoring;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.neoforged.neoforge.common.Tags;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import xyz.bluspring.kilt.injections.world.item.DyeColorInjection;

@Mixin(ShulkerBoxColoring.class)
public abstract class ShulkerBoxColoringInject {
    @Definition(id = "itemStack", local = @Local(type = ItemStack.class))
    @Definition(id = "getItem", method = "Lnet/minecraft/world/item/ItemStack;getItem()Lnet/minecraft/world/item/Item;")
    @Definition(id = "DyeItem", type = DyeItem.class)
    @Expression("itemStack.getItem() instanceof DyeItem")
    @ModifyExpressionValue(method = "matches(Lnet/minecraft/world/inventory/CraftingContainer;Lnet/minecraft/world/level/Level;)Z", at = @At("MIXINEXTRAS:EXPRESSION"))
    private boolean kilt$checkIsDyeTag(boolean original, @Local ItemStack stack) {
        return original || stack.is(Tags.Items.DYES);
    }

    @Definition(id = "byItem", method = "Lnet/minecraft/world/level/block/Block;byItem(Lnet/minecraft/world/item/Item;)Lnet/minecraft/world/level/block/Block;")
    @Definition(id = "ShulkerBoxBlock", type = ShulkerBoxBlock.class)
    @Expression("byItem(?) instanceof ShulkerBoxBlock")
    @ModifyExpressionValue(method = "assemble(Lnet/minecraft/world/inventory/CraftingContainer;Lnet/minecraft/core/RegistryAccess;)Lnet/minecraft/world/item/ItemStack;", at = @At("MIXINEXTRAS:EXPRESSION"))
    private boolean kilt$tryStoreDyeColor(boolean original, @Share("dyeColor") LocalRef<DyeColor> dyeColor, @Local(ordinal = 1) ItemStack stack) {
        if (!original) {
            var tmp = DyeColorInjection.getColor(stack);

            if (tmp != null)
                dyeColor.set(tmp);
        }

        return original;
    }

    @ModifyArg(method = "assemble(Lnet/minecraft/world/inventory/CraftingContainer;Lnet/minecraft/core/RegistryAccess;)Lnet/minecraft/world/item/ItemStack;", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/ShulkerBoxBlock;getColoredItemStack(Lnet/minecraft/world/item/DyeColor;)Lnet/minecraft/world/item/ItemStack;"))
    private @Nullable DyeColor kilt$trySetCustomDyeColor(@Nullable DyeColor color, @Share("dyeColor") LocalRef<DyeColor> dyeColor) {
        if (dyeColor.get() != null)
            return dyeColor.get();

        return color;
    }
}
