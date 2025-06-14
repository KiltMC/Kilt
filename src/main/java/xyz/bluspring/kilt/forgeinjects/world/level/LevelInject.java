// TRACKED HASH: 7ec032f8735b23aa563858eb4a3555caa9c5d7ff
package xyz.bluspring.kilt.forgeinjects.world.level;

import com.bawnorton.mixinsquared.TargetHandler;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.capabilities.CapabilityProvider;
import net.minecraftforge.common.capabilities.ICapabilityProviderImpl;
import net.minecraftforge.common.extensions.IForgeBlockState;
import net.minecraftforge.common.extensions.IForgeLevel;
import net.minecraftforge.common.util.BlockSnapshot;
import net.minecraftforge.entity.PartEntity;
import net.minecraftforge.event.ForgeEventFactory;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.bluspring.kilt.helpers.mixin.Extends;
import xyz.bluspring.kilt.injections.CapabilityProviderInjection;
import xyz.bluspring.kilt.injections.world.level.LevelInjection;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

@Mixin(value = Level.class, priority = 1111) // higher priority to mixin to Porting Lib
@Extends(CapabilityProvider.class)
public abstract class LevelInject implements CapabilityProviderInjection, ICapabilityProviderImpl<Level>, IForgeLevel, LevelInjection {
    public boolean restoringBlockSnapshots = false;
    public boolean captureBlockSnapshots = false;

    @Unique private final ArrayList<BlockEntity> freshBlockEntities = new ArrayList<>();
    @Unique private final ArrayList<BlockEntity> pendingFreshBlockEntities = new ArrayList<>();

    @Shadow @Final public boolean isClientSide;

    @Shadow public abstract ResourceKey<Level> dimension();

    @Shadow public abstract BlockState getBlockState(BlockPos blockPos);

    @Shadow public abstract void updateNeighbourForOutputSignal(BlockPos pos, Block block);

    @Shadow @Final private ResourceKey<Level> dimension;
    private double maxEntityRadius = 2.0D;
    @Override
    public double getMaxEntityRadius() {
        return maxEntityRadius;
    }

    @Override
    public double increaseMaxEntityRadius(double value) {
        if (value > maxEntityRadius)
            maxEntityRadius = value;
        return maxEntityRadius;
    }

    public ArrayList<BlockSnapshot> capturedBlockSnapshots = new ArrayList<>();

    @Override
    public ArrayList<BlockSnapshot> getCapturedBlockSnapshots() {
        return capturedBlockSnapshots;
    }

    @Inject(method = "setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;II)Z", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;getBlock()Lnet/minecraft/world/level/block/Block;", ordinal = 0, shift = At.Shift.AFTER))
    private void kilt$captureSnapshot(BlockPos pos, BlockState state, int flags, int recursionLeft, CallbackInfoReturnable<Boolean> cir, @Local(argsOnly = true) LocalRef<BlockPos> posRef, @Share("blockSnapshot") LocalRef<BlockSnapshot> blockSnapshot) {
        posRef.set(pos.immutable());

        if (this.captureBlockSnapshots && !this.isClientSide) {
            blockSnapshot.set(BlockSnapshot.create(this.dimension, (Level) (Object) this, posRef.get()));
            this.capturedBlockSnapshots.add(blockSnapshot.get());
        }

        // TODO: Kilt: what are these used for?
        BlockState old = this.getBlockState(posRef.get());
        int oldLight = old.getLightEmission((Level) (Object) this, posRef.get());
        int oldOpacity = old.getLightBlock((Level) (Object) this, posRef.get());
    }

    @Inject(method = "setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;II)Z", at = @At(value = "RETURN", ordinal = 2))
    private void kilt$removeCapturedSnapshot(BlockPos pos, BlockState state, int flags, int recursionLeft, CallbackInfoReturnable<Boolean> cir, @Share("blockSnapshot") LocalRef<BlockSnapshot> blockSnapshot) {
        if (blockSnapshot.get() != null) {
            this.capturedBlockSnapshots.remove(blockSnapshot.get());
        }
    }

    @Override
    public void kilt$setCapturingBlockSnapshots(boolean value) {
        this.captureBlockSnapshots = value;
    }

    @Override
    public void kilt$setRestoringBlockSnapshots(boolean value) {
        this.restoringBlockSnapshots = value;
    }

    @Override
    public boolean kilt$getCapturingBlockSnapshots() {
        return this.captureBlockSnapshots;
    }

    @Override
    public boolean kilt$getRestoringBlockSnapshots() {
        return this.restoringBlockSnapshots;
    }

    @Inject(method = "setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;II)Z", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;getBlockState(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/state/BlockState;"), cancellable = true)
    private void kilt$cancelIfCapturing(BlockPos pos, BlockState state, int flags, int recursionLeft, CallbackInfoReturnable<Boolean> cir, @Share("blockSnapshot") LocalRef<BlockSnapshot> blockSnapshot) {
        if (blockSnapshot.get() != null) {
            cir.setReturnValue(true);
        }
    }

    @Redirect(method = "updateNeighbourForOutputSignal", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;is(Lnet/minecraft/world/level/block/Block;)Z", ordinal = 0))
    public boolean kilt$checkForNeighbourChange(BlockState instance, Block unused, BlockPos blockPos, @Local(index = 1) BlockPos directionPos) {
        ((IForgeBlockState) instance).onNeighborChange((Level) (Object) this, directionPos, blockPos);
        // Don't trigger the Vanilla neighbour change.
        return false;
    }

    @Redirect(method = "updateNeighbourForOutputSignal", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/block/state/BlockState;is(Lnet/minecraft/world/level/block/Block;)Z", ordinal = 1))
    public boolean kilt$getWeakChange(BlockState instance, Block unused, BlockPos blockPos, Block block, @Local(index = 1) BlockPos directionPos) {
        return ((IForgeBlockState) instance).getWeakChanges((Level) (Object) this, directionPos);
    }

    @Inject(method = "blockEntityChanged", at = @At("TAIL"))
    public void kilt$updateNeighbourOutputSignalsForChange(BlockPos pos, CallbackInfo ci) {
        this.updateNeighbourForOutputSignal(pos, this.getBlockState(pos).getBlock());
    }

    @Inject(method = "removeBlockEntity", at = @At("TAIL"))
    public void kilt$updateNeighbourOutputSignalsForRemoval(BlockPos pos, CallbackInfo ci) {
        this.updateNeighbourForOutputSignal(pos, this.getBlockState(pos).getBlock());
    }

    @Inject(method = "updateNeighborsAt", at = @At("TAIL"))
    public void kilt$notifyNeighbours(BlockPos pos, Block block, CallbackInfo ci) {
        // why is "isCanceled()" added at the end?
        ForgeEventFactory.onNeighborNotify((Level) (Object) this, pos, this.getBlockState(pos), EnumSet.allOf(Direction.class), false).isCanceled();
    }

    @SuppressWarnings("UnresolvedMixinReference")
    @TargetHandler(mixin = "io.github.fabricators_of_create.porting_lib.mixin.common.LevelMixin", name = "port_lib$onBlockEntitiesLoad")
    @Redirect(method = "@MixinSquared:Handler", at = @At(value = "INVOKE", target = "Ljava/util/ArrayList;forEach(Ljava/util/function/Consumer;)V", remap = false))
    private void kilt$loadBlockEntitiesForge(ArrayList<BlockEntity> instance, Consumer<BlockEntity> consumer) {
        instance.forEach(BlockEntity::onLoad);
    }

    @Inject(method = "getEntities(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/phys/AABB;Ljava/util/function/Predicate;)Ljava/util/List;", at = @At("TAIL"))
    private void kilt$addPartEntitiesToList(Entity entity, AABB area, Predicate<? super Entity> predicate, CallbackInfoReturnable<List<Entity>> cir, @Local List<Entity> list) {
        for (PartEntity<?> partEntity : this.kilt$getPartEntities()) {
            if (partEntity != entity && partEntity.getBoundingBox().intersects(area) && predicate.test(partEntity)) {
                list.add(partEntity);
            }
        }
    }

    @Inject(method = "getEntities(Lnet/minecraft/world/level/entity/EntityTypeTest;Lnet/minecraft/world/phys/AABB;Ljava/util/function/Predicate;Ljava/util/List;I)V", at = @At("TAIL"))
    private <T extends Entity> void kilt$addPartEntitiesToList(EntityTypeTest<Entity, T> entityTypeTest, AABB bounds, Predicate<? super T> predicate, List<? super T> output, int maxResults, CallbackInfo ci) {
        for (PartEntity<?> partEntity : this.kilt$getPartEntities()) {
            var castEntity = entityTypeTest.tryCast(partEntity);
            if (castEntity != null && partEntity.getBoundingBox().intersects(bounds) && predicate.test(castEntity)) {
                // TODO: Kilt: doesn't this technically overflow the maxResults?
                output.add(castEntity);
                if (output.size() >= maxResults)
                    break;
            }
        }
    }
}