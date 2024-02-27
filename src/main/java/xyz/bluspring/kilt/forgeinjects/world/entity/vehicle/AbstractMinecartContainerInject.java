// TRACKED HASH: 55954f29283374374411e03d1687d92a59065c5a
package xyz.bluspring.kilt.forgeinjects.world.entity.vehicle;

import net.minecraft.core.Direction;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.vehicle.AbstractMinecart;
import net.minecraft.world.entity.vehicle.AbstractMinecartContainer;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.wrapper.InvWrapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(AbstractMinecartContainer.class)
public abstract class AbstractMinecartContainerInject extends AbstractMinecart {
    protected AbstractMinecartContainerInject(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    private LazyOptional<?> itemHandler = LazyOptional.of(() -> new InvWrapper((AbstractMinecartContainer) (Object) this));

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (this.isAlive() && cap == ForgeCapabilities.ITEM_HANDLER)
            return itemHandler.cast();

        return super.getCapability(cap, side);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        itemHandler.invalidate();
    }

    @Override
    public void reviveCaps() {
        super.reviveCaps();
        itemHandler = LazyOptional.of(() -> new InvWrapper((AbstractMinecartContainer) (Object) this));
    }
}