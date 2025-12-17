// TRACKED HASH: a1f8c952c92e35e0c9d786ea3cad6f768d53a153
package xyz.bluspring.kilt.injects.world.level.block;

import com.google.common.collect.Maps;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FlowerPotBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.bluspring.kilt.helpers.mixin.CreateInitializer;
import xyz.bluspring.kilt.injections.world.level.block.FlowerPotBlockInjection;
import xyz.bluspring.kilt.util.KiltHelper;

import java.util.Collections;
import java.util.Map;
import java.util.function.Supplier;

@Mixin(FlowerPotBlock.class)
public abstract class FlowerPotBlockInject extends Block implements FlowerPotBlockInjection {
    @Shadow @Final @Mutable private Block potted;
    @Shadow public abstract Block getPotted();

    @Unique private Map<ResourceLocation, Supplier<? extends Block>> fullPots = Maps.newHashMap();
    @Unique private Supplier<FlowerPotBlock> emptyPot;
    @Unique private Supplier<? extends Block> flowerDelegate;

    public FlowerPotBlockInject(Properties properties) {
        super(properties);
    }

    @CreateInitializer
    public FlowerPotBlockInject(@Nullable Supplier<FlowerPotBlock> emptyPot, Supplier<? extends Block> block, BlockBehaviour.Properties properties) {
        super(properties);
        this.potted = null;
        this.flowerDelegate = block;
        if (emptyPot == null) {
            this.fullPots = Maps.newHashMap();
            this.emptyPot = null;
        } else {
            this.fullPots = Collections.emptyMap();
            this.emptyPot = emptyPot;
        }
    }

    // This isn't a part of Forge itself (coremods aside), but it needs to be done in order to
    // make sure the Vanilla checks are able to actually have flower pots function properly with Forge.
    @Inject(method = "getCloneItemStack", at = @At("HEAD"))
    public void kilt$cacheContents(LevelReader level, BlockPos pos, BlockState state, CallbackInfoReturnable<ItemStack> cir) {
        this.getPotted();
    }

    @Inject(method = "useWithoutItem", at = @At("HEAD"))
    public void kilt$cacheContents(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult, CallbackInfoReturnable<InteractionResult> cir) {
        this.getPotted();
    }

    @Inject(method = "useItemOn", at = @At("HEAD"))
    public void kilt$cacheContents(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult, CallbackInfoReturnable<ItemInteractionResult> cir) {
        this.getPotted();
    }

    @Inject(method = "isEmpty", at = @At("HEAD"))
    public void kilt$cacheContents(CallbackInfoReturnable<Boolean> cir) {
        this.getPotted();
    }

    @Inject(method = "getPotted", at = @At("HEAD"))
    private void kilt$deferredContentGet(CallbackInfoReturnable<Block> cir) {
        if (this.potted == null && this.flowerDelegate != null) {
            this.potted = this.flowerDelegate.get();
        }
    }

    @WrapOperation(method = "useItemOn", at = @At(value = "INVOKE", target = "Ljava/util/Map;getOrDefault(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;"))
    private <K, V> V kilt$tryUseCustomEmptyPotCheck(Map<K, V> instance, Object key, V defaultValue, Operation<V> original) {
        if (this.emptyPot != null || KiltHelper.INSTANCE.hasMethodOverride(this.getClass(), FlowerPotBlock.class, "getEmptyPot")) {
            return (V) this.getEmptyPot().getFullPotsView().getOrDefault(BuiltInRegistries.BLOCK.getKey((Block) key), () -> (Block) defaultValue).get();
        }

        return original.call(instance, key, defaultValue);
    }

    @ModifyExpressionValue(method = "useWithoutItem", at = @At(value = "FIELD", target = "Lnet/minecraft/world/level/block/Blocks;FLOWER_POT:Lnet/minecraft/world/level/block/Block;"))
    private Block kilt$useCustomEmptyPot(Block original) {
        if (original != Blocks.FLOWER_POT) // Kilt: Prioritize Fabric mods
            return original;

        if (this.emptyPot != null || KiltHelper.INSTANCE.hasMethodOverride(this.getClass(), FlowerPotBlock.class, "getEmptyPot")) {
            return this.getEmptyPot();
        }

        return original;
    }

    public FlowerPotBlock getEmptyPot() {
        return emptyPot == null ? (FlowerPotBlock) (Object) this : emptyPot.get();
    }

    @Override
    public void addPlant(ResourceLocation flower, Supplier<? extends Block> fullPot) {
        if (getEmptyPot() != (Object) this) {
            throw new IllegalArgumentException("Cannot add plant to non-empty pot: " + this);
        }
        fullPots.put(flower, fullPot);
    }

    public Map<ResourceLocation, Supplier<? extends Block>> getFullPotsView() {
        return Collections.unmodifiableMap(fullPots);
    }
}