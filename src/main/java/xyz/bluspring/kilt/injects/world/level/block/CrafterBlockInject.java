package xyz.bluspring.kilt.injects.world.level.block;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.neoforged.neoforge.items.VanillaInventoryCodeHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.CrafterBlock;
import net.minecraft.world.level.block.entity.CrafterBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

@Mixin(CrafterBlock.class)
public abstract class CrafterBlockInject extends BaseEntityBlock {
    protected CrafterBlockInject(Properties properties) {
        super(properties);
    }

    @Definition(id = "itemStack", local = @Local(type = ItemStack.class, ordinal = 1))
    @Definition(id = "isEmpty", method = "Lnet/minecraft/world/item/ItemStack;isEmpty()Z")
    @Expression("itemStack.isEmpty() == 0")
    @Inject(method = "dispenseItem", at = @At("MIXINEXTRAS:EXPRESSION"), slice = @Slice(from = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;copy()Lnet/minecraft/world/item/ItemStack;", ordinal = 0), to = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;isEmpty()Z", ordinal = 3)))
    private void kilt$storeHasHandledOutput(CallbackInfo ci, @Share("kilt$handledOutput") LocalBooleanRef handledOutputRef) {
        handledOutputRef.set(true);
    }

    @Inject(method = "dispenseItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;isEmpty()Z", ordinal = 3))
    private void kilt$tryHandleCrafterOutput(ServerLevel level, BlockPos pos, CrafterBlockEntity crafter, ItemStack stack, BlockState state, RecipeHolder<CraftingRecipe> recipe, CallbackInfo ci, @Share("kilt$handledOutput") LocalBooleanRef handledOutputRef, @Local(ordinal = 1) LocalRef<ItemStack> outputStack) {
        if (!handledOutputRef.get()) {
            outputStack.set(VanillaInventoryCodeHooks.insertCrafterOutput(level, pos, crafter, outputStack.get()));
        }
    }
}
