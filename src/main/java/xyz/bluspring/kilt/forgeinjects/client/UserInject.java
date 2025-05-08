package xyz.bluspring.kilt.forgeinjects.client;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.PropertyMap;
import net.minecraft.client.User;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import xyz.bluspring.kilt.injections.client.UserInjection;

@Mixin(User.class)
public abstract class UserInject implements UserInjection {
    @Unique private PropertyMap properties;

    @Override
    public void setProperties(PropertyMap properties) {
        if (this.properties == null)
            this.properties = properties;
    }

    @Override
    public boolean hasCachedProperties() {
        return this.properties != null;
    }

    @ModifyReturnValue(method = "getGameProfile", at = @At("RETURN"))
    private GameProfile kilt$storeCachedProfileProperties(GameProfile original) {
        if (this.properties != null)
            original.getProperties().putAll(this.properties);

        return original;
    }
}
