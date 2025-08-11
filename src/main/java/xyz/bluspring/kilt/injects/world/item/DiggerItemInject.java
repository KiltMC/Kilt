package xyz.bluspring.kilt.injects.world.item;

import net.minecraft.tags.TagKey;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TieredItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.TierSortingRegistry;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(DiggerItem.class)
public abstract class DiggerItemInject extends TieredItem {
    @Shadow @Final private TagKey<Block> blocks;

    public DiggerItemInject(Tier tier, Properties properties) {
        super(tier, properties);
    }

    @Inject(method = "isCorrectToolForDrops", at = @At("HEAD"), cancellable = true)
    private void kilt$checkIsCorrectTool(BlockState block, CallbackInfoReturnable<Boolean> cir) {
        if (TierSortingRegistry.isTierSorted(this.getTier())) {
            cir.setReturnValue(TierSortingRegistry.isCorrectTierForDrops(this.getTier(), block) && block.is(this.blocks));
        }
    }

    @Override
    public boolean isCorrectToolForDrops(ItemStack stack, BlockState state) {
        return state.is(this.blocks) && TierSortingRegistry.isCorrectTierForDrops(this.getTier(), state);
    }
}
