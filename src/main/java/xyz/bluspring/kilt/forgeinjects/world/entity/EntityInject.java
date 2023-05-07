package xyz.bluspring.kilt.forgeinjects.world.entity;

import io.github.fabricators_of_create.porting_lib.extensions.EntityExtensions;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityDispatcher;
import net.minecraftforge.common.capabilities.CapabilityProvider;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.extensions.IForgeEntity;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.fluids.FluidType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import xyz.bluspring.kilt.injections.CapabilityProviderInjection;
import xyz.bluspring.kilt.injections.capabilities.EntityCapabilityProviderImpl;
import xyz.bluspring.kilt.workarounds.CapabilityProviderWorkaround;

import java.util.function.BiPredicate;
import java.util.function.Supplier;

@Mixin(Entity.class)
public abstract class EntityInject implements IForgeEntity, CapabilityProviderInjection, EntityCapabilityProviderImpl, EntityExtensions {
    @Shadow public Level level;

    @Shadow public abstract float getBbWidth();

    @Shadow public abstract float getBbHeight();

    @Shadow protected abstract void unsetRemoved();

    private final CapabilityProviderWorkaround<Entity> workaround = new CapabilityProviderWorkaround<>(Entity.class, (Entity) (Object) this);

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        return workaround.getCapability(cap, side);
    }

    @Override
    public CapabilityProviderWorkaround<Entity> getWorkaround() {
        return workaround;
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

    private CompoundTag persistentData;

    @Override
    public CompoundTag getPersistentData() {
        if (persistentData == null)
            persistentData = new CompoundTag();

        return persistentData;
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
    public boolean areCapsCompatible(CapabilityProvider<Entity> other) {
        return workaround.areCapsCompatible(other);
    }

    @Override
    public boolean areCapsCompatible(@Nullable CapabilityDispatcher other) {
        return workaround.areCapsCompatible(other);
    }

    @Override
    public void invalidateCaps() {
        workaround.invalidateCaps();
    }

    @Override
    public void reviveCaps() {
        workaround.reviveCaps();
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

    @Override
    public void gatherCapabilities(@Nullable Supplier<ICapabilityProvider> parent) {
        workaround.invokeGatherCapabilities(parent);
    }
}
