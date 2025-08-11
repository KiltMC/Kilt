package xyz.bluspring.kilt.injects.client.multiplayer.resolver;

import net.minecraft.client.multiplayer.resolver.AddressCheck;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(AddressCheck.class)
public interface AddressCheckInject {
    // Kilt: this patch only actually makes it load under the module class loader, which Kilt intentionally avoids using entirely.
}
