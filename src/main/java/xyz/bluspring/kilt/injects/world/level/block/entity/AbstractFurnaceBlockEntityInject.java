package xyz.bluspring.kilt.injects.world.level.block.entity;

import java.util.function.ObjIntConsumer;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.datafixers.util.Either;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.bluspring.kilt.helpers.mixin.CreateStatic;
import xyz.bluspring.kilt.injections.world.level.block.entity.AbstractFurnaceBlockEntityInjection;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.FuelValues;
import net.minecraft.world.level.block.state.BlockState;

@Mixin(AbstractFurnaceBlockEntity.class)
public abstract class AbstractFurnaceBlockEntityInject extends BaseContainerBlockEntity implements AbstractFurnaceBlockEntityInjection {
    @Shadow
    private int cookingTotalTime;

    @Shadow
    private static int getTotalCookTime(ServerLevel level, AbstractFurnaceBlockEntity entity) {
        throw new UnsupportedOperationException("Implemented via mixin");
    }

    @Shadow
    private int cookingTimer;
    @Unique
    private RecipeType<? extends AbstractCookingRecipe> recipeType;

    protected AbstractFurnaceBlockEntityInject(BlockEntityType<?> type, BlockPos worldPosition, BlockState blockState) {
        super(type, worldPosition, blockState);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void kilt$storeRecipeType(BlockEntityType<?> type, BlockPos pos, BlockState blockState, RecipeType<? extends AbstractCookingRecipe> recipeType, CallbackInfo ci) {
        this.recipeType = recipeType;
    }

    @ModifyReturnValue(method = "getBurnDuration", at = @At("RETURN"))
    private int kilt$tryUseCustomFuel(int original, @Local(argsOnly = true) ItemStack stack, @Local(argsOnly = true, name = "fuelValues") FuelValues fuelValues) {
        if (original != 0) {
            return original;
        }
        return stack.getBurnTime(recipeType, fuelValues);
    }

    @Unique private boolean needsCookingReset = false;
    @Unique private final SnapshotJournal<Boolean> cookingResetJournal = new SnapshotJournal<Boolean>() {
        @Override
        protected Boolean createSnapshot() {
            return needsCookingReset;
        }

        @Override
        protected void revertToSnapshot(Boolean snapshot) {
            needsCookingReset = snapshot;
        }

        @Override
        protected void onRootCommit(Boolean originalState) {
            if (needsCookingReset) {
                if (level instanceof ServerLevel serverLevel) {
                    cookingTotalTime = getTotalCookTime(serverLevel, (AbstractFurnaceBlockEntity) (Object) AbstractFurnaceBlockEntityInject.this); // pray.
                    cookingTimer = 0;
                }

                needsCookingReset = false;
            }
        }
    };

    @Override
    public void onTransfer(int slot, int amountChange, TransactionContext transaction) {
        if (slot == 0) {
            int currentAmount = this.getItem(slot).getCount();

            if (currentAmount == 0 || currentAmount == amountChange) {
                cookingResetJournal.updateSnapshots(transaction);
                needsCookingReset = true;
            }
        }
    }

    @WrapOperation(method = "canPlaceItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/entity/FuelValues;isFuel(Lnet/minecraft/world/item/ItemStack;)Z"))
    private boolean kilt$checkIsCustomFuel(FuelValues instance, ItemStack itemStack, Operation<Boolean> original) {
        return original.call(instance, itemStack) || itemStack.getBurnTime(this.recipeType, instance) > 0;
    }

    @CreateStatic
    private static void buildFuels(ObjIntConsumer<Either<Item, TagKey<Item>>> fuelConsumer) {
        AbstractFurnaceBlockEntityInjection.buildFuels(fuelConsumer);
    }
}
