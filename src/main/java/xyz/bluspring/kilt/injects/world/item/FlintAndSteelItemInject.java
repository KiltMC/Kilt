package xyz.bluspring.kilt.injects.world.item;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.common.ItemAbility;
import net.neoforged.neoforge.common.extensions.IBlockExtension;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.bluspring.kilt.util.KiltHelper;

import net.minecraft.core.BlockPos;import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.FlintAndSteelItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

@Mixin(FlintAndSteelItem.class)
public abstract class FlintAndSteelItemInject extends Item {
    public FlintAndSteelItemInject(Properties properties) {
        super(properties);
    }

    @Definition(id = "blockState", local = @Local(type = BlockState.class))
    @Definition(id = "level", local = @Local(type = Level.class))
    @Definition(id = "getBlockState", method = "Lnet/minecraft/world/level/Level;getBlockState(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/state/BlockState;")
    @Definition(id = "blockPos", local = @Local(type = BlockPos.class))
    @Expression("blockState = level.getBlockState(blockPos)")
    @Inject(method = "useOn", at = @At(value = "MIXINEXTRAS:EXPRESSION", shift = At.Shift.AFTER))
    private void kilt$getToolModifiedState(UseOnContext context, CallbackInfoReturnable<InteractionResult> cir, @Local BlockState state, @Share("blockstate2") LocalRef<BlockState> stateRef) {
        stateRef.set(state.getToolModifiedState(context, ItemAbilities.FIRESTARTER_LIGHT, false));
    }

    @Definition(id = "canLight", method = "Lnet/minecraft/world/level/block/CandleCakeBlock;canLight(Lnet/minecraft/world/level/block/state/BlockState;)Z")
    @Definition(id = "blockState", local = @Local(type = BlockState.class))
    @Expression("canLight(blockState) == 0")
    @ModifyExpressionValue(method = "useOn", at = @At("MIXINEXTRAS:EXPRESSION"))
    private boolean kilt$checkHasToolModified(boolean original, @Share("blockstate2") LocalRef<BlockState> stateRef) {
        return original && stateRef.get() == null;
    }

    @Definition(id = "blockState", local = @Local(type = BlockState.class))
    @Definition(id = "setValue", method = "Lnet/minecraft/world/level/block/state/BlockState;setValue(Lnet/minecraft/world/level/block/state/properties/Property;Ljava/lang/Comparable;)Ljava/lang/Object;")
    @Definition(id = "LIT", field = "Lnet/minecraft/world/level/block/state/properties/BlockStateProperties;LIT:Lnet/minecraft/world/level/block/state/properties/BooleanProperty;")
    @Definition(id = "BlockState", type = BlockState.class)
    @Expression("(BlockState) blockState.setValue(LIT, true)")
    @ModifyExpressionValue(method = "useOn", at = @At("MIXINEXTRAS:EXPRESSION"))
    private BlockState kilt$tryUseCustomState(BlockState original, @Local BlockState state, @Share("blockstate2") LocalRef<BlockState> stateRef) {
        if (KiltHelper.INSTANCE.hasMethodOverride(state.getBlock().getClass(), IBlockExtension.class, "getToolModifiedState", UseOnContext.class, ItemAbility.class, boolean.class)) {
            return stateRef.get();
        }

        return original;
    }

    @Override
    public boolean canPerformAction(ItemStack stack, ItemAbility itemAbility) {
        return ItemAbilities.DEFAULT_FLINT_ACTIONS.contains(itemAbility);
    }
}
