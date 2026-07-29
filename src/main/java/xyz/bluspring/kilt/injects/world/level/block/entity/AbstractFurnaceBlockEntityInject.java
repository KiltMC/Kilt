package xyz.bluspring.kilt.injects.world.level.block.entity;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.datafixers.util.Either;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.bluspring.kilt.helpers.mixin.CreateStatic;
import xyz.bluspring.kilt.injections.world.level.block.entity.AbstractFurnaceBlockEntityInjection;

import java.util.Map;
import java.util.function.ObjIntConsumer;

@Mixin(AbstractFurnaceBlockEntity.class)
public abstract class AbstractFurnaceBlockEntityInject implements AbstractFurnaceBlockEntityInjection {
    @Shadow
    private static boolean canBurn(RegistryAccess registryAccess, @Nullable RecipeHolder<?> recipeHolder, NonNullList<ItemStack> nonNullList, int i) {
        throw new UnsupportedOperationException("Implemented via mixin");
    }

    @Inject(method = "add(Ljava/util/Map;Lnet/minecraft/world/level/ItemLike;I)V", at = @At("TAIL"))
    private static void kilt$appendItemToKiltMap(Map<Item, Integer> map, ItemLike item, int burnTime, CallbackInfo ci) {
        AbstractFurnaceBlockEntityInjection.kilt$itemCookTimes.put(item, burnTime);
    }

    @Inject(method = "add(Ljava/util/Map;Lnet/minecraft/tags/TagKey;I)V", at = @At("TAIL"))
    private static void kilt$appendTagToKiltMap(Map<Item, Integer> map, TagKey<Item> itemTag, int burnTime, CallbackInfo ci) {
        AbstractFurnaceBlockEntityInjection.kilt$tagCookTimes.put(itemTag, burnTime);
    }

    @Unique
    private RecipeType<? extends AbstractCookingRecipe> recipeType;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void kilt$storeRecipeType(BlockEntityType<?> type, BlockPos pos, BlockState blockState, RecipeType<? extends AbstractCookingRecipe> recipeType, CallbackInfo ci) {
        this.recipeType = recipeType;
    }

    @ModifyReturnValue(method = "getBurnDuration", at = @At("RETURN"))
    private int kilt$tryUseCustomFuel(int original, @Local(argsOnly = true) ItemStack stack) {
        if (original != 0) {
            return original;
        }
        return stack.getBurnTime(recipeType);
    }

    @ModifyReturnValue(method = "isFuel", at = @At("RETURN"))
    private static boolean kilt$checkIsCustomFuel(boolean original, @Local(argsOnly = true) ItemStack stack) {
        return original || stack.getBurnTime(null) > 0;
    }

    @CreateStatic
    private static void buildFuels(ObjIntConsumer<Either<Item, TagKey<Item>>> fuelConsumer) {
        AbstractFurnaceBlockEntityInjection.buildFuels(fuelConsumer);
    }

    @Unique
    private static final ThreadLocal<AbstractFurnaceBlockEntity> kilt$currentFurnace = ThreadLocal.withInitial(() -> null);

    @CreateStatic
    private static boolean canBurn(RegistryAccess registryAccess, @Nullable RecipeHolder<?> recipe, NonNullList<ItemStack> inventory, int maxStackSize, AbstractFurnaceBlockEntity furnace) {
        kilt$currentFurnace.set(furnace);
        var result = canBurn(registryAccess, recipe, inventory, maxStackSize);
        kilt$currentFurnace.set(null);
        return result;
    }

    @WrapOperation(
        method = "canBurn",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/world/item/crafting/Recipe;getResultItem(Lnet/minecraft/core/HolderLookup$Provider;)Lnet/minecraft/world/item/ItemStack;"
        )
    )
    private static ItemStack kilt$checkCanBurnWithFurnace(
        Recipe<?> recipe, HolderLookup.Provider provider, Operation<ItemStack> original, @Local(argsOnly = true) RecipeHolder<?> recipeHolder
    ) {
        if (kilt$currentFurnace.get() != null) {
            return ((RecipeHolder<? extends AbstractCookingRecipe>) recipeHolder).value().assemble(new SingleRecipeInput(kilt$currentFurnace.get().getItem(0)), provider);
        }
        return original.call(recipe, provider);
    }
}
