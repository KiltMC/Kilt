package xyz.bluspring.kilt.forgeinjects.world;

import net.minecraft.world.MenuProvider;
import net.neoforged.neoforge.client.extensions.IMenuProviderExtension;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(MenuProvider.class)
public abstract class MenuProviderInject implements IMenuProviderExtension {
}
