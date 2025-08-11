// TRACKED HASH: a21056576fc73d9bb232df2f44147986d9f61768
package xyz.bluspring.kilt.injects.world.item;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.ToolAction;
import net.neoforged.neoforge.common.ToolActions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import xyz.bluspring.kilt.helpers.mixin.CreateStatic;
import xyz.bluspring.kilt.injections.item.AxeItemInjection;

import java.util.Optional;
import java.util.function.Function;

@Mixin(AxeItem.class)
public abstract class AxeItemInject extends DiggerItem implements AxeItemInjection {
    public AxeItemInject(float attackDamageModifier, float attackSpeedModifier, Tier tier, TagKey<Block> blocks, Properties properties) {
        super(attackDamageModifier, attackSpeedModifier, tier, blocks, properties);
    }

    @WrapOperation(method = "useOn", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/AxeItem;getStripped(Lnet/minecraft/world/level/block/state/BlockState;)Ljava/util/Optional;"))
    private Optional<BlockState> kilt$tryUseToolModifiedState(AxeItem instance, BlockState unstrippedState, Operation<Optional<BlockState>> original, @Local(argsOnly = true) UseOnContext context) {
        var optional = original.call(instance, unstrippedState);

        if (optional.isPresent()) {
            return optional;
        }

        return Optional.ofNullable(unstrippedState.getToolModifiedState(context, ToolActions.AXE_STRIP, false));
    }

    @WrapOperation(method = "useOn", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/WeatheringCopper;getPrevious(Lnet/minecraft/world/level/block/state/BlockState;)Ljava/util/Optional;"))
    private Optional<BlockState> kilt$tryUseToolModifiedState(BlockState state, Operation<Optional<BlockState>> original, @Local(ordinal = 0) Optional<BlockState> optional1, @Local(argsOnly = true) UseOnContext context) {
        var optional = original.call(state);

        if (optional.isPresent())
            return optional;

        if (optional1.isPresent())
            return Optional.empty();

        return Optional.ofNullable(state.getToolModifiedState(context, ToolActions.AXE_SCRAPE, false));
    }

    @WrapOperation(method = "useOn", at = @At(value = "INVOKE", target = "Ljava/util/Optional;map(Ljava/util/function/Function;)Ljava/util/Optional;"))
    private <T, U> Optional<BlockState> kilt$tryUseToolModifiedState(Optional<BlockState> instance, Function<? super T, ? extends U> mapper, Operation<Optional<U>> original, @Local BlockState state, @Local(ordinal = 0) Optional<BlockState> optional1, @Local(ordinal = 1) Optional<BlockState> optional2, @Local(argsOnly = true) UseOnContext context) {
        if (instance.isPresent())
            return (Optional<BlockState>) original.call(instance, mapper);

        if (optional1.isPresent() || optional2.isPresent())
            return Optional.empty();

        return Optional.ofNullable(state.getToolModifiedState(context, ToolActions.AXE_WAX_OFF, false));
    }

    @CreateStatic
    private static BlockState getAxeStrippingState(BlockState originalState) {
        return AxeItemInjection.getAxeStrippingState(originalState);
    }

    @Override
    public boolean canPerformAction(ItemStack stack, ToolAction toolAction) {
        return ToolActions.DEFAULT_AXE_ACTIONS.contains(toolAction);
    }

    @Override
    public boolean canDisableShield(ItemStack stack, ItemStack shield, LivingEntity entity, LivingEntity attacker) {
        return true;
    }
}