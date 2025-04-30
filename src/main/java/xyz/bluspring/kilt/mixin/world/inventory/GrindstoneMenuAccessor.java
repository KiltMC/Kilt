package xyz.bluspring.kilt.mixin.world.inventory;

import net.minecraft.world.Container;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.GrindstoneMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(GrindstoneMenu.class)
public interface GrindstoneMenuAccessor {
    @Accessor
    Container getRepairSlots();

    @Accessor
    ContainerLevelAccess getAccess();
}
