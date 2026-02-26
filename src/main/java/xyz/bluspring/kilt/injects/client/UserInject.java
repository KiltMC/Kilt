package xyz.bluspring.kilt.injects.client;

import net.minecraft.client.User;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(User.class)
public abstract class UserInject {
    // Kilt: no
}
