// TRACKED HASH: 38fad16390c9ef6678740c522678f54eb006b7cd
package xyz.bluspring.kilt.forgeinjects.world.entity.vehicle;

import net.minecraft.core.Direction;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.vehicle.AbstractMinecartContainer;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.entity.vehicle.ChestBoat;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.wrapper.InvWrapper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ChestBoat.class)
public abstract class ChestBoatInject extends Boat {
    private LazyOptional<?> itemHandler = LazyOptional.of(() -> new InvWrapper((AbstractMinecartContainer) (Object) this));

    public ChestBoatInject(EntityType<? extends Boat> entityType, Level level) {
        super(entityType, level);
    }

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