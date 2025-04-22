package xyz.bluspring.kilt.forgeinjects.client;

import net.minecraft.client.ClientBrandRetriever;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(ClientBrandRetriever.class)
public class ClientBrandRetrieverInject {
    // Kilt: this is only here to satisfy the patch progress, this isn't really needed or important.
    //       otherwise, it would probably look like "fabric+kilt", which i don't really want.
}
