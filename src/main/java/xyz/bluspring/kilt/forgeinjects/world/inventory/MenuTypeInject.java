package xyz.bluspring.kilt.forgeinjects.world.inventory;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.network.IContainerFactory;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(MenuType.class)
public abstract class MenuTypeInject<T extends AbstractContainerMenu> implements IForgeMenuType<T> {
    @Shadow @Final private MenuType.MenuSupplier<T> constructor;

    @Shadow public abstract T create(int containerId, Inventory playerInventory);

    @Override
    public T create(int windowId, Inventory playerInv, FriendlyByteBuf extraData) {
        if (this.constructor instanceof IContainerFactory) {
            return ((IContainerFactory<T>) this.constructor).create(windowId, playerInv, extraData);
        }

        return this.create(windowId, playerInv);
    }
}
