// TRACKED HASH: 00999822b8acf90ce2c48fea9dad5cfee4bcdd4e
package xyz.bluspring.kilt.forgeinjects.world.inventory;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.network.IContainerFactory;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(MenuType.class)
public abstract class MenuTypeInject<T extends AbstractContainerMenu> implements IMenuTypeExtension<T> {
    @Shadow @Final private MenuType.MenuSupplier<T> constructor;

    @Shadow public abstract T create(int containerId, Inventory playerInventory);

    @Override
    public T create(int windowId, Inventory playerInv, RegistryFriendlyByteBuf extraData) {
        if (this.constructor instanceof IContainerFactory) {
            return ((IContainerFactory<T>) this.constructor).create(windowId, playerInv, extraData);
        }

        return this.create(windowId, playerInv);
    }
}