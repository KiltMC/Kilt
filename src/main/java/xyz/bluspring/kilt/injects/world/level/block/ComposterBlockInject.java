package xyz.bluspring.kilt.injects.world.level.block;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import it.unimi.dsi.fastutil.objects.Object2FloatMap;
import org.spongepowered.asm.mixin.Intrinsic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.bluspring.kilt.helpers.mixin.CreateStatic;
import xyz.bluspring.kilt.injections.world.level.block.ComposterBlockInjection;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ComposterBlock;
import net.minecraft.world.level.block.state.BlockState;

@Mixin(ComposterBlock.class)
public abstract class ComposterBlockInject extends Block implements ComposterBlockInjection {
    public ComposterBlockInject(Properties properties) {
        super(properties);
    }

    @Inject(method = "onPlace", at = @At("TAIL"))
    private void kilt$invalidateComposterCapabilities(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston, CallbackInfo ci) {
        if (!oldState.is(this)) {
            level.invalidateCapabilities(pos);
        }
    }

    @Intrinsic
    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        super.onRemove(state, level, pos, newState, movedByPiston);
        if (!state.is(newState.getBlock())) {
            level.invalidateCapabilities(pos);
        }
    }

    @Definition(id = "COMPOSTABLES", field = "Lnet/minecraft/world/level/block/ComposterBlock;COMPOSTABLES:Lit/unimi/dsi/fastutil/objects/Object2FloatMap;")
    @Definition(id = "containsKey", method = "Lit/unimi/dsi/fastutil/objects/Object2FloatMap;containsKey(Ljava/lang/Object;)Z")
    @Expression("COMPOSTABLES.containsKey(?)")
    @ModifyExpressionValue(method = {"useItemOn", "insertItem"}, at = @At("MIXINEXTRAS:EXPRESSION"))
    private static boolean kilt$tryUseDataMap(boolean original, @Local(argsOnly = true) ItemStack stack) {
        return original || getValue(stack) > 0;
    }

    @Definition(id = "COMPOSTABLES", field = "Lnet/minecraft/world/level/block/ComposterBlock;COMPOSTABLES:Lit/unimi/dsi/fastutil/objects/Object2FloatMap;")
    @Definition(id = "getFloat", method = "Lit/unimi/dsi/fastutil/objects/Object2FloatMap;getFloat(Ljava/lang/Object;)F")
    @Expression("COMPOSTABLES.getFloat(?)")
    @WrapOperation(method = "addItem", at = @At("MIXINEXTRAS:EXPRESSION"))
    private static <T> float kilt$tryUseDataMap(Object2FloatMap<T> instance, T o, Operation<Float> original, @Local(argsOnly = true) ItemStack stack) {
        var value = getValue(stack);

        if (value > 0) {
            return value;
        }

        return original.call(instance, o);
    }

    @Mixin(targets = "net/minecraft/world/level/block/ComposterBlock$InputContainer")
    public abstract static class InputContainerInject {
        @Definition(id = "COMPOSTABLES", field = "Lnet/minecraft/world/level/block/ComposterBlock;COMPOSTABLES:Lit/unimi/dsi/fastutil/objects/Object2FloatMap;")
        @Definition(id = "containsKey", method = "Lit/unimi/dsi/fastutil/objects/Object2FloatMap;containsKey(Ljava/lang/Object;)Z")
        @Expression("COMPOSTABLES.containsKey(?)")
        @ModifyExpressionValue(method = "canPlaceItemThroughFace", at = @At("MIXINEXTRAS:EXPRESSION"))
        private static boolean kilt$tryUseDataMap(boolean original, @Local(argsOnly = true) ItemStack stack) {
            return original || ComposterBlockInjection.getValue(stack) > 0;
        }
    }

    @CreateStatic
    private static float getValue(ItemStack item) {
        return ComposterBlockInjection.getValue(item);
    }
}
