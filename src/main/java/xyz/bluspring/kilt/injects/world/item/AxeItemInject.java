// TRACKED HASH: a21056576fc73d9bb232df2f44147986d9f61768
package xyz.bluspring.kilt.injects.world.item;

import java.util.Optional;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.common.ItemAbility;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import xyz.bluspring.kilt.helpers.mixin.CreateStatic;
import xyz.bluspring.kilt.injections.world.item.AxeItemInjection;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemInstance;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

@Mixin(AxeItem.class)
public abstract class AxeItemInject extends Item implements AxeItemInjection {
    private ThreadLocal<UseOnContext> kilt$useOnContext = ThreadLocal.withInitial(() -> null);

    public AxeItemInject(Properties properties) {
        super(properties);
    }

    @WrapOperation(method = "useOn", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/AxeItem;evaluateNewBlockState(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/level/block/state/BlockState;)Ljava/util/Optional;"))
    private Optional<BlockState> kilt$tryUseToolModifiedState(AxeItem instance, Level level, BlockPos pos, Player player, BlockState state, Operation<Optional<BlockState>> original, @Local(argsOnly = true) UseOnContext context) {
        kilt$useOnContext.set(context);
        var optional = original.call(instance, level, pos, player, state);
        kilt$useOnContext.remove();
        return optional;
    }

    @WrapOperation(method = "evaluateNewBlockState", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/AxeItem;getStripped(Lnet/minecraft/world/level/block/state/BlockState;)Ljava/util/Optional;"))
    private Optional<BlockState> tryToUseStrippedAbility(AxeItem instance, BlockState unstrippedState, Operation<Optional<BlockState>> original) {
        var optional = original.call(instance, unstrippedState);
        if (optional.isPresent())
            return optional;

        return Optional.ofNullable(unstrippedState.getToolModifiedState(kilt$useOnContext.get(), ItemAbilities.AXE_STRIP, false));
    }

    @WrapOperation(method = "evaluateNewBlockState", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/WeatheringCopper;getPrevious(Lnet/minecraft/world/level/block/state/BlockState;)Ljava/util/Optional;"))
    private Optional<BlockState> tryToUseScrapeAbility(BlockState state, Operation<Optional<BlockState>> original) {
        var optional = original.call(state);

        if (optional.isPresent())
            return optional;

        return Optional.ofNullable(state.getToolModifiedState(kilt$useOnContext.get(), ItemAbilities.AXE_SCRAPE, false));
    }

    @WrapOperation(method = "evaluateNewBlockState", at = @At(value = "INVOKE", target = "Ljava/util/Optional;ofNullable(Ljava/lang/Object;)Ljava/util/Optional;"))
    private Optional<BlockState> tryToUseWaxOffAbility(Object o, Operation<Optional<BlockState>> original, @Local(argsOnly = true) BlockState state) {
        var optional = original.call(o);
        if (optional.isPresent())
            return optional;

        return Optional.ofNullable(state.getToolModifiedState(kilt$useOnContext.get(), ItemAbilities.AXE_WAX_OFF, false));
    }

    @CreateStatic
    private static BlockState getAxeStrippingState(BlockState originalState) {
        return AxeItemInjection.getAxeStrippingState(originalState);
    }

    @Override
    public boolean canPerformAction(ItemInstance stack, ItemAbility toolAction) {
        return ItemAbilities.DEFAULT_AXE_ACTIONS.contains(toolAction);
    }
}
