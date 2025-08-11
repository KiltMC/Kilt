// TRACKED HASH: 4585164eec037ac883eaba1534904974ae174569
package xyz.bluspring.kilt.injects.world.level.block.entity;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.CommonHooks;
import net.neoforged.neoforge.common.capabilities.Capability;
import net.neoforged.neoforge.common.capabilities.ForgeCapabilities;
import net.neoforged.neoforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.SidedInvWrapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.bluspring.kilt.injections.world.level.block.entity.AbstractFurnaceBlockEntityInjection;

@Mixin(AbstractFurnaceBlockEntity.class)
public abstract class AbstractFurnaceBlockEntityInject extends BaseContainerBlockEntity implements AbstractFurnaceBlockEntityInjection {
    @Shadow private static boolean canBurn(RegistryAccess registryAccess, @Nullable Recipe<?> recipe, NonNullList<ItemStack> nonNullList, int i) {
        throw new IllegalStateException();
    }

    @Shadow private static boolean burn(RegistryAccess registryAccess, @Nullable Recipe<?> recipe, NonNullList<ItemStack> nonNullList, int i) {
        throw new IllegalStateException();
    }

    private RecipeType<? extends AbstractCookingRecipe> recipeType;

    protected AbstractFurnaceBlockEntityInject(BlockEntityType<?> blockEntityType, BlockPos blockPos, BlockState blockState) {
        super(blockEntityType, blockPos, blockState);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void kilt$initRecipeType(BlockEntityType<?> blockEntityType, BlockPos blockPos, BlockState blockState, RecipeType<? extends AbstractCookingRecipe> recipeType, CallbackInfo ci) {
        this.recipeType = recipeType;
    }

    @Unique private static final ThreadLocal<AbstractFurnaceBlockEntity> kilt$furnaceBE = new ThreadLocal<>();

    @WrapOperation(method = "serverTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/entity/AbstractFurnaceBlockEntity;canBurn(Lnet/minecraft/core/RegistryAccess;Lnet/minecraft/world/item/crafting/Recipe;Lnet/minecraft/core/NonNullList;I)Z"))
    private static boolean kilt$useForgeCanBurn(RegistryAccess registryAccess, @Nullable Recipe<?> recipe, NonNullList<ItemStack> nonNullList, int i, Operation<Boolean> original, @Local(argsOnly = true) AbstractFurnaceBlockEntity furnaceBlockEntity) {
        kilt$furnaceBE.set(furnaceBlockEntity);
        var value = original.call(registryAccess, recipe, nonNullList, i);
        kilt$furnaceBE.remove();
        return value;
    }

    @WrapOperation(method = "serverTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/entity/AbstractFurnaceBlockEntity;burn(Lnet/minecraft/core/RegistryAccess;Lnet/minecraft/world/item/crafting/Recipe;Lnet/minecraft/core/NonNullList;I)Z"))
    private static boolean kilt$useForgeBurn(RegistryAccess registryAccess, @Nullable Recipe<?> recipe, NonNullList<ItemStack> nonNullList, int i, Operation<Boolean> original, @Local(argsOnly = true) AbstractFurnaceBlockEntity furnaceBlockEntity) {
        kilt$furnaceBE.set(furnaceBlockEntity);
        var value = original.call(registryAccess, recipe, nonNullList, i);
        kilt$furnaceBE.remove();
        return value;
    }

    @Override
    public boolean forge$canBurn(RegistryAccess registryAccess, @Nullable Recipe<?> recipe, NonNullList<ItemStack> items, int maxStackSize) {
        kilt$furnaceBE.set((AbstractFurnaceBlockEntity) (Object) this);
        var shouldBurn = canBurn(registryAccess, recipe, items, maxStackSize);
        kilt$furnaceBE.remove();
        return shouldBurn;
    }

    @WrapOperation(method = {"canBurn", "burn"}, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/crafting/Recipe;getResultItem(Lnet/minecraft/core/RegistryAccess;)Lnet/minecraft/world/item/ItemStack;"))
    private static ItemStack kilt$tryUseForgeAssemble(Recipe<?> instance, RegistryAccess registryAccess, Operation<ItemStack> original) {
        if (kilt$furnaceBE.get() != null) {
            return ((Recipe<WorldlyContainer>) instance).assemble(kilt$furnaceBE.get(), registryAccess);
        }

        return original.call(instance, registryAccess);
    }

    @Override
    public boolean forge$burn(RegistryAccess registryAccess, @Nullable Recipe<?> recipe, NonNullList<ItemStack> items, int maxStackSize) {
        kilt$furnaceBE.set((AbstractFurnaceBlockEntity) (Object) this);
        var shouldBurn = burn(registryAccess, recipe, items, maxStackSize);
        kilt$furnaceBE.remove();
        return shouldBurn;
    }

    @ModifyExpressionValue(method = "getBurnDuration", at = @At(value = "INVOKE", target = "Ljava/util/Map;getOrDefault(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;"))
    private <V> V kilt$useForgeBurnTime(V original, @Local(argsOnly = true) ItemStack stack) {
        if (original instanceof Integer integer) {
            if (integer == 0)
                return (V) (Object) CommonHooks.getBurnTime(stack, this.recipeType);
        }

        return original;
    }

    @ModifyReturnValue(method = "isFuel", at = @At("RETURN"))
    private static boolean kilt$checkForgeBurnTime(boolean original, @Local(argsOnly = true) ItemStack stack) {
        return original || CommonHooks.getBurnTime(stack, null) > 0;
    }

    @ModifyExpressionValue(method = "canPlaceItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/entity/AbstractFurnaceBlockEntity;isFuel(Lnet/minecraft/world/item/ItemStack;)Z"))
    private boolean kilt$checkForgeBurnTimeOnPlaceItem(boolean original, @Local(argsOnly = true) ItemStack stack) {
        return original || CommonHooks.getBurnTime(stack, this.recipeType) > 0;
    }

    LazyOptional<? extends IItemHandler>[] handlers = SidedInvWrapper.create((AbstractFurnaceBlockEntity) (Object) this, Direction.UP, Direction.DOWN, Direction.NORTH);

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (!this.remove && side != null && cap == ForgeCapabilities.ITEM_HANDLER) {
            if (side == Direction.UP)
                return handlers[0].cast();
            else if (side == Direction.DOWN)
                return handlers[1].cast();
            else
                return handlers[2].cast();
            }
        return super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        for (LazyOptional<? extends IItemHandler> handler : handlers) {
            handler.invalidate();
        }
    }

    @Override
    public void reviveCaps() {
        super.reviveCaps();
        this.handlers = SidedInvWrapper.create((AbstractFurnaceBlockEntity) (Object) this, Direction.UP, Direction.DOWN, Direction.NORTH);
    }
}