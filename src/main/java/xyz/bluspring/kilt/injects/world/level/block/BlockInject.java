package xyz.bluspring.kilt.injects.world.level.block;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.IdMapper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.extensions.common.IClientBlockExtensions;
import net.neoforged.neoforge.common.CommonHooks;
import net.neoforged.neoforge.registries.GameData;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.bluspring.kilt.injections.world.level.block.BlockInjection;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

@Mixin(Block.class)
public abstract class BlockInject implements BlockInjection {
    @Shadow @Final @Mutable public static IdMapper<BlockState> BLOCK_STATE_REGISTRY;

    @Inject(method = "<clinit>", at = @At("TAIL"))
    private static void kilt$useBlockStateIdMap(CallbackInfo ci) {
        BLOCK_STATE_REGISTRY = GameData.getBlockStateIDMap();
    }

    @WrapOperation(method = "shouldRenderFace", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;skipRendering(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/Direction;)Z"))
    private static boolean kilt$checkSupportsExternalHiding(BlockState instance, BlockState blockState, Direction direction, Operation<Boolean> original, @Local(argsOnly = true) BlockGetter level, @Local(argsOnly = true, ordinal = 1) BlockPos pos) {
        return original.call(instance, blockState, direction) || (blockState.hidesNeighborFace(level, pos, instance, direction.getOpposite()) && instance.supportsExternalFaceHiding());
    }

    @Inject(method = "dropResources(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/Block;getDrops(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/entity/BlockEntity;)Ljava/util/List;"))
    private static void kilt$beginCapturingDrops(BlockState state, Level level, BlockPos pos, CallbackInfo ci) {
        kilt$beginCapturingDrops();
    }

    @WrapOperation(method = "dropResources(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;spawnAfterBreak(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/item/ItemStack;Z)V"))
    private static void kilt$handleBlockDrops(BlockState instance, ServerLevel level, BlockPos pos, ItemStack stack, boolean b, Operation<Void> original) {
        var captured = kilt$stopCapturingDrops();
        CommonHooks.kilt$handleBlockDrops(level, pos, instance, null, captured, null, ItemStack.EMPTY, () -> original.call(instance, level, pos, stack, b)); // Kilt TODO: do we need to pass false?
    }

    @Inject(method = "dropResources(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/LevelAccessor;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/entity/BlockEntity;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/Block;getDrops(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/entity/BlockEntity;)Ljava/util/List;"))
    private static void kilt$beginCapturingDrops(BlockState state, LevelAccessor level, BlockPos pos, BlockEntity blockEntity, CallbackInfo ci) {
        kilt$beginCapturingDrops();
    }

    @WrapOperation(method = "dropResources(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/entity/BlockEntity;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/item/ItemStack;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;spawnAfterBreak(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/item/ItemStack;Z)V"))
    private static void kilt$handleBlockDrops(BlockState instance, ServerLevel level, BlockPos pos, ItemStack stack, boolean b, Operation<Void> original, @Local(argsOnly = true) BlockEntity blockEntity) {
        var captured = kilt$stopCapturingDrops();
        CommonHooks.kilt$handleBlockDrops(level, pos, instance, blockEntity, captured, null, ItemStack.EMPTY, () -> original.call(instance, level, pos, stack, b)); // Kilt TODO: do we need to pass false?
    }

    @Inject(method = "dropResources(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/entity/BlockEntity;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/item/ItemStack;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/Block;getDrops(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/entity/BlockEntity;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/item/ItemStack;)Ljava/util/List;"))
    private static void kilt$beginCapturingDrops(BlockState state, Level level, BlockPos pos, BlockEntity blockEntity, Entity entity, ItemStack tool, CallbackInfo ci) {
        kilt$beginCapturingDrops();
    }

    @WrapOperation(method = "dropResources(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/entity/BlockEntity;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/item/ItemStack;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;spawnAfterBreak(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/item/ItemStack;Z)V"))
    private static void kilt$handleBlockDrops(BlockState instance, ServerLevel level, BlockPos pos, ItemStack stack, boolean b, Operation<Void> original, @Local(argsOnly = true) BlockEntity blockEntity, @Local(argsOnly = true) Entity entity) {
        var captured = kilt$stopCapturingDrops();
        CommonHooks.kilt$handleBlockDrops(level, pos, instance, blockEntity, captured, entity, ItemStack.EMPTY, () -> original.call(instance, level, pos, stack, b)); // Kilt TODO: do we need to pass false?
    }

    @ModifyExpressionValue(method = "popResource(Lnet/minecraft/world/level/Level;Ljava/util/function/Supplier;Lnet/minecraft/world/item/ItemStack;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/GameRules;getBoolean(Lnet/minecraft/world/level/GameRules$Key;)Z"))
    private static boolean kilt$checkIsRestoringBlockSnapshots(boolean original, @Local(argsOnly = true) Level level) {
        return original && !level.kilt$getRestoringBlockSnapshots();
    }

    @WrapOperation(method = "popResource(Lnet/minecraft/world/level/Level;Ljava/util/function/Supplier;Lnet/minecraft/world/item/ItemStack;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z"))
    private static boolean kilt$tryCaptureResourceDrops(Level instance, Entity entity, Operation<Boolean> original) {
        if (capturedDrops != null && entity instanceof ItemEntity itemEntity) {
            capturedDrops.put(itemEntity, () -> original.call(instance, entity));
        }

        return original.call(instance, entity);
    }

    @ModifyExpressionValue(method = "popExperience", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/GameRules;getBoolean(Lnet/minecraft/world/level/GameRules$Key;)Z"))
    private boolean kilt$checkIsRestoringBlockSnapshots(boolean original, @Local(argsOnly = true) ServerLevel level) {
        return original && !level.kilt$getRestoringBlockSnapshots();
    }

    // Kilt: We're using a map instead, mod compatibility time.
    @Unique @Nullable
    private static Map<ItemEntity, Runnable> capturedDrops = null;

    @Unique
    private static void kilt$beginCapturingDrops() {
        capturedDrops = new HashMap<>();
    }

    @Unique
    private static Map<ItemEntity, Runnable> kilt$stopCapturingDrops() {
        Map<ItemEntity, Runnable> drops = capturedDrops;
        capturedDrops = null;
        return drops;
    }

    @Override
    public void initializeClient(Consumer<IClientBlockExtensions> consumer) {
    }
}
