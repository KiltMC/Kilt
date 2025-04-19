package xyz.bluspring.kilt.forgeinjects.world.entity;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.github.fabricators_of_create.porting_lib.extensions.EntityExtensions;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.CapabilityProvider;
import net.minecraftforge.common.extensions.IForgeEntity;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.fluids.FluidType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.bluspring.kilt.helpers.mixin.Extends;
import xyz.bluspring.kilt.injections.CapabilityProviderInjection;
import xyz.bluspring.kilt.injections.capabilities.EntityCapabilityProviderImpl;
import xyz.bluspring.kilt.injections.world.entity.EntityInjection;

import java.util.function.BiPredicate;

@Mixin(Entity.class)
@Extends(CapabilityProvider.class)
public abstract class EntityInject implements IForgeEntity, CapabilityProviderInjection, EntityCapabilityProviderImpl, EntityExtensions, EntityInjection {
    @Shadow public Level level;

    @Shadow public abstract float getBbWidth();

    @Shadow public abstract float getBbHeight();

    @Shadow protected abstract void unsetRemoved();

    @Shadow protected abstract float getEyeHeight(Pose pose, EntityDimensions dimensions);

    @Shadow private EntityDimensions dimensions;

    @Shadow public abstract boolean isRemoved();

    @Shadow public abstract void discard();

    @WrapOperation(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;getEyeHeight(Lnet/minecraft/world/entity/Pose;Lnet/minecraft/world/entity/EntityDimensions;)F"))
    private float kilt$useSizesFromEvent(Entity instance, Pose pose, EntityDimensions dimensions, Operation<Float> original) {
        var event = ForgeEventFactory.getEntitySizeForge(instance, pose, dimensions, original.call(instance, pose, dimensions));

        this.dimensions = event.getNewSize();
        return event.getNewEyeHeight();
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void kilt$callForgeEntityInitEvents(EntityType<?> entityType, Level level, CallbackInfo ci) {
        MinecraftForge.EVENT_BUS.post(new EntityEvent.EntityConstructing((Entity) (Object) this));
        this.gatherCapabilities();
    }

    @Inject(method = "remove", at = @At("TAIL"))
    private void kilt$invalidateEntityCapabilities(Entity.RemovalReason reason, CallbackInfo ci) {
        this.invalidateCaps();
    }

    private boolean canUpdate = true;

    @Override
    public boolean canUpdate() {
        return canUpdate;
    }

    @Override
    public void canUpdate(boolean value) {
        canUpdate = value;
    }

    @Override
    public CompoundTag getPersistentData() {
        return this.getExtraCustomData(); // Kilt: use Porting Lib's
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public boolean canTrample(BlockState state, BlockPos pos, float fallDistance) {
        return this.level.random.nextFloat() < fallDistance - .5F
                && ((Object) this) instanceof LivingEntity
                && (((Object) this) instanceof Player || ForgeEventFactory.getMobGriefingEvent(this.level, ((Entity) (Object) this)))
                && this.getBbWidth() * this.getBbWidth() * this.getBbHeight() > .512F;
    }

    private boolean isAddedToWorld;

    @Override
    public boolean isAddedToWorld() {
        return isAddedToWorld;
    }

    @Override
    public void onAddedToWorld() {
        isAddedToWorld = true;
    }

    @Override
    public void onRemovedFromWorld() {
        isAddedToWorld = false;
    }

    @Override
    public void revive() {
        this.unsetRemoved();
        this.reviveCaps();
    }

    // TODO: Implement these
    @Override
    public double getFluidTypeHeight(FluidType type) {
        return 0;
    }

    @Override
    public FluidType getMaxHeightFluidType() {
        return null;
    }

    @Override
    public boolean isInFluidType(BiPredicate<FluidType, Double> predicate, boolean forAllTypes) {
        return false;
    }

    @Override
    public boolean isInFluidType() {
        return false;
    }

    @Override
    public FluidType getEyeInFluidType() {
        return null;
    }

    @Override
    public float getEyeHeightAccess(Pose pose, EntityDimensions dimensions) {
        return this.getEyeHeight(pose, dimensions);
    }

    @Redirect(method = "spawnAtLocation(Lnet/minecraft/world/item/ItemStack;F)Lnet/minecraft/world/entity/item/ItemEntity;", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z"))
    public boolean kilt$captureSpawnDrops(Level instance, Entity entity) {
        if (captureDrops() != null) {
            captureDrops().add((ItemEntity) entity);
            return false;
        } else {
            return instance.addFreshEntity(entity);
        }
    }
}
