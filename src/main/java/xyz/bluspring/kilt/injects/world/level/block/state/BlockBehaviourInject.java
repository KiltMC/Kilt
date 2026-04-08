package xyz.bluspring.kilt.injects.world.level.block.state;

import java.util.function.BiConsumer;
import java.util.function.Supplier;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.extensions.IBlockExtension;
import net.neoforged.neoforge.event.EventHooks;
import net.neoforged.neoforge.event.entity.player.UseItemOnBlockEvent;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.bluspring.kilt.injections.world.level.block.state.BlockBehaviourInjection;
import xyz.bluspring.kilt.mixin.BlockBehaviourPropertiesAccessor;
import xyz.bluspring.kilt.util.KiltHelper;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.phys.BlockHitResult;

@Mixin(BlockBehaviour.class)
public abstract class BlockBehaviourInject implements BlockBehaviourInjection {
    @Shadow
    protected abstract Block asBlock();

    @Shadow
    @Nullable
    protected ResourceKey<LootTable> drops;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void kilt$storeLootTableFromCache(BlockBehaviour.Properties properties, CallbackInfo ci) {
        final ResourceKey<LootTable> lootTableCache = ((BlockBehaviourPropertiesAccessor) properties).getDrops();
        if (lootTableCache != null) {
            this.lootTableSupplier = () -> lootTableCache;
        } else if (properties.getLootTableSupplier() != null) {
            this.lootTableSupplier = properties.getLootTableSupplier();
        } else {
            this.lootTableSupplier = null;
        }
    }

    @Unique private Supplier<ResourceKey<LootTable>> lootTableSupplier = null;

    @WrapOperation(method = "onExplosionHit", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/Block;dropFromExplosion(Lnet/minecraft/world/level/Explosion;)Z"))
    private boolean kilt$checkCanDropFromExplosion(Block instance, Explosion explosion, Operation<Boolean> original, @Local(argsOnly = true) BlockState state, @Local(argsOnly = true) Level level, @Local(argsOnly = true) BlockPos pos) {
        if (KiltHelper.INSTANCE.hasMethodOverrideWithReturnType(instance.getClass(), IBlockExtension.class, "canDropFromExplosion", boolean.class, BlockState.class, BlockGetter.class, BlockPos.class, Explosion.class)) {
            return instance.canDropFromExplosion(state, level, pos, explosion);
        }

        return original.call(instance, explosion);
    }

    @Inject(method = "onExplosionHit", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;I)Z"))
    private void kilt$tryUseNeoBlockExploded(BlockState state, Level level, BlockPos pos, Explosion explosion, BiConsumer<ItemStack, BlockPos> dropConsumer, CallbackInfo ci) {
        if (KiltHelper.INSTANCE.hasMethodOverride(state.getBlock().getClass(), IBlockExtension.class, "onBlockExploded")) {
            state.onBlockExploded(level, pos, explosion);
        }
    }

    @WrapOperation(method = "onExplosionHit", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;I)Z"))
    private boolean kilt$avoidCallIfOverridden(Level instance, BlockPos pos, BlockState newState, int flags, Operation<Boolean> original, @Local(argsOnly = true) BlockState state) {
        if (KiltHelper.INSTANCE.hasMethodOverride(state.getBlock().getClass(), IBlockExtension.class, "onBlockExploded"))
            return false;

        return original.call(instance, pos, newState, flags);
    }

    @WrapOperation(method = "onExplosionHit", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/Block;wasExploded(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/Explosion;)V"))
    private void kilt$avoidCallIfOverridden(Block instance, Level level, BlockPos pos, Explosion explosion, Operation<Void> original, @Local(argsOnly = true) BlockState state) {
        if (KiltHelper.INSTANCE.hasMethodOverride(state.getBlock().getClass(), IBlockExtension.class, "onBlockExploded"))
            return;

        original.call(instance, level, pos, explosion);
    }

    @WrapOperation(method = "getDestroyProgress", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;hasCorrectToolForDrops(Lnet/minecraft/world/level/block/state/BlockState;)Z"))
    private boolean kilt$checkCanPlayerHarvest(Player instance, BlockState state, Operation<Boolean> original, @Local(argsOnly = true) BlockGetter level, @Local(argsOnly = true) BlockPos pos) {
        return EventHooks.kilt$doPlayerHarvestCheck(instance, state, level, pos, original.call(instance, state));
    }

    @WrapOperation(method = "getDestroyProgress", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;getDestroySpeed(Lnet/minecraft/world/level/block/state/BlockState;)F"))
    private float kilt$tryUseCustomDigSpeed(Player instance, BlockState state, Operation<Float> original, @Local(argsOnly = true) BlockPos pos) {
        instance.kilt$storeDugBlockPos(pos);
        var value = original.call(instance, state);
        instance.kilt$storeDugBlockPos(null); // just in case

        return value;
    }

    @Inject(method = "getLootTable", at = @At("HEAD"))
    private void kilt$tryUseLootTableSupplierForCache(CallbackInfoReturnable<ResourceKey<LootTable>> cir) {
        if (this.drops == null && this.lootTableSupplier != null) {
            this.drops = this.lootTableSupplier.get();
        }
    }

    public boolean isAir(BlockState state) {
        if (state instanceof BlockStateBaseInjection injection) {
            return injection.kilt$isAir();
        }

        return state.isAir();
    }

    @Mixin(BlockBehaviour.BlockStateBase.class)
    public abstract static class BlockStateBaseInject implements BlockBehaviourInjection.BlockStateBaseInjection {
        @Shadow
        @Final
        private boolean isAir;

        @Shadow
        public abstract Block getBlock();

        @Shadow
        protected abstract BlockState asState();

        @Override
        public boolean kilt$isAir() {
            return this.isAir;
        }

        @Inject(method = "isAir", at = @At("HEAD"), cancellable = true)
        private void kilt$checkIsAirViaBlock(CallbackInfoReturnable<Boolean> cir) {
            if (KiltHelper.INSTANCE.hasMethodOverride(this.getBlock().getClass(), IBlockExtension.class, "isAir", BlockState.class)) {
                cir.setReturnValue(this.getBlock().isAir(this.asState()));
            }
        }

        @ModifyReturnValue(method = "getMapColor", at = @At("RETURN"))
        private MapColor kilt$tryUseCustomMapColor(MapColor original, @Local(argsOnly = true) BlockGetter level, @Local(argsOnly = true) BlockPos pos) {
            return this.getBlock().getMapColor(this.asState(), level, pos, original);
        }

        @Inject(method = "getPistonPushReaction", at = @At("HEAD"), cancellable = true)
        private void kilt$tryUsePistonPushReaction(CallbackInfoReturnable<PushReaction> cir) {
            var reaction = this.getBlock().getPistonPushReaction(this.asState());
            if (reaction != null)
                cir.setReturnValue(reaction);
        }

        @Inject(method = "useItemOn", at = @At("HEAD"), cancellable = true)
        private void kilt$tryHandleUseItemOnBlock(ItemStack stack, Level level, Player player, InteractionHand hand, BlockHitResult hitResult, CallbackInfoReturnable<ItemInteractionResult> cir) {
            var useOnContext = new UseOnContext(level, player, hand, stack, hitResult);
            var e = NeoForge.EVENT_BUS.post(new UseItemOnBlockEvent(useOnContext, UseItemOnBlockEvent.UsePhase.BLOCK));
            if (e.isCanceled()) {
                cir.setReturnValue(e.getCancellationResult());
            }
        }
    }

    @Mixin(BlockBehaviour.Properties.class)
    public static abstract class PropertiesInject implements BlockBehaviourInjection.PropertiesInjection {
        @Unique private Supplier<ResourceKey<LootTable>> lootTableSupplier;

        @WrapOperation(method = "method_26239", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;getLightEmission()I"))
        private static int kilt$tryUseCustomLightEmission(BlockState instance, Operation<Integer> original, @Local(argsOnly = true) BlockGetter level, @Local(argsOnly = true) BlockPos pos) {
            if (KiltHelper.INSTANCE.hasMethodOverride(instance.getBlock().getClass(), IBlockExtension.class, "getLightEmission", BlockState.class, BlockGetter.class, BlockPos.class)) {
                return instance.getLightEmission(level, pos);
            }

            return original.call(instance);
        }

        @Override
        public Supplier<ResourceKey<LootTable>> getLootTableSupplier() {
            return this.lootTableSupplier;
        }

        @Override
        public BlockBehaviour.Properties lootFrom(Supplier<? extends Block> blockIn) {
            this.lootTableSupplier = () -> blockIn.get().getLootTable();
            return (BlockBehaviour.Properties) (Object) this;
        }
    }
}
