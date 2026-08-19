package xyz.bluspring.kilt.injects.world.item;

import org.spongepowered.asm.mixin.Implements;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Intrinsic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import xyz.bluspring.kilt.injections.world.item.BlockItemInjection;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.SolidBucketItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

@Implements(@Interface(iface = BlockItemInjection.class, prefix = "kilt$i$"))
@Mixin(SolidBucketItem.class)
public abstract class SolidBucketItemInject extends BlockItem {
    @Shadow protected abstract SoundEvent getPlaceSound(BlockState state);

    public SolidBucketItemInject(Block block, Properties properties) {
        super(block, properties);
    }

    @Intrinsic(displace = true)
    public SoundEvent kilt$i$getPlaceSound(BlockState state, Level world, BlockPos pos, Player entity) {
        return this.getPlaceSound(state);
    }
}
