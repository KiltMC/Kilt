package xyz.bluspring.kilt.mixin.compat.forge.moonlight;

import net.mehvahdjukaar.moonlight.api.events.IFireConsumeBlockEvent;
import net.mehvahdjukaar.moonlight.api.events.MoonlightEventsHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.FireBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// Reimplements Moonlight's mixin but for Fabric
@Mixin(FireBlock.class)
public abstract class FireBlockMixin extends BaseFireBlock {
    public FireBlockMixin(Properties properties, float fireDamage) {
        super(properties, fireDamage);
    }

    @SuppressWarnings("MixinAnnotationTarget")
    @Shadow(remap = false) @Final private ThreadLocal<Direction> kilt$face;
    @Unique private BlockState bs;

    @Inject(method = "checkBurnOut", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;removeBlock(Lnet/minecraft/core/BlockPos;Z)Z", shift = At.Shift.AFTER))
    private void afterRemoveBlock(Level level, BlockPos pos, int chance, RandomSource pRandom, int age, CallbackInfo ci) {
        IFireConsumeBlockEvent event = IFireConsumeBlockEvent.create(pos, level, this.bs, chance, age, kilt$face.get());
        MoonlightEventsHelper.postEvent(event, IFireConsumeBlockEvent.class);
        BlockState newState = event.getFinalState();
        if (newState != null) {
            level.setBlockAndUpdate(pos, newState);
        }
    }

    @Inject(method = "checkBurnOut", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;removeBlock(Lnet/minecraft/core/BlockPos;Z)Z"))
    private void beforeRemoveBlock(Level level, BlockPos pos, int chance, RandomSource random, int age, CallbackInfo ci) {
        this.bs = level.getBlockState(pos);
    }
}
