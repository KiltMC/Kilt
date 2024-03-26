// TRACKED HASH: a1f8c952c92e35e0c9d786ea3cad6f768d53a153
package xyz.bluspring.kilt.forgeinjects.world.level.block;

import com.google.common.collect.Maps;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FlowerPotBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.bluspring.kilt.helpers.mixin.CreateInitializer;
import xyz.bluspring.kilt.injections.world.level.block.FlowerPotBlockInjection;

import java.util.Collections;
import java.util.Map;
import java.util.function.Supplier;

@Mixin(FlowerPotBlock.class)
public abstract class FlowerPotBlockInject extends Block implements FlowerPotBlockInjection {
    @Shadow public Block content;

    @Shadow public abstract Block getContent();

    @Unique
    private Map<net.minecraft.resources.ResourceLocation, java.util.function.Supplier<? extends Block>> fullPots = Maps.newHashMap();
    @Unique
    private Supplier<FlowerPotBlock> emptyPot;
    @Unique
    private Supplier<? extends Block> flowerDelegate;

    public FlowerPotBlockInject(Properties properties) {
        super(properties);
    }

    @CreateInitializer
    public FlowerPotBlockInject(@Nullable Supplier<FlowerPotBlock> emptyPot, Supplier<? extends Block> block, BlockBehaviour.Properties properties) {
        super(properties);
        this.content = null; // apparently this is redirected? i should really support coremods, huh
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
    public void kilt$cacheContents(BlockGetter level, BlockPos pos, BlockState state, CallbackInfoReturnable<ItemStack> cir) {
        this.getContent();
    }

    @Inject(method = "use", at = @At("HEAD"))
    public void kilt$cacheContents(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit, CallbackInfoReturnable<InteractionResult> cir) {
        this.getContent();
    }

    @Inject(method = "isEmpty", at = @At("HEAD"))
    public void kilt$cacheContents(CallbackInfoReturnable<Boolean> cir) {
        this.getContent();
    }

    @Inject(method = "getContent", at = @At("HEAD"))
    private void kilt$deferredContentGet(CallbackInfoReturnable<Block> cir) {
        if (this.content == null && this.flowerDelegate != null) {
            this.content = this.flowerDelegate.get();
        }
    }

    public FlowerPotBlock getEmptyPot() {
        return emptyPot == null ? (FlowerPotBlock) (Object) this : emptyPot.get();
    }

    public void addPlant(ResourceLocation flower, Supplier<? extends Block> fullPot) {
        if (getEmptyPot() != (Object) this) {
            throw new IllegalArgumentException("Cannot add plant to non-empty pot: " + this);
        }
        fullPots.put(flower, fullPot);
    }

    public Map<ResourceLocation, Supplier<? extends Block>> getFullPotsView() {
        return java.util.Collections.unmodifiableMap(fullPots);
    }
}