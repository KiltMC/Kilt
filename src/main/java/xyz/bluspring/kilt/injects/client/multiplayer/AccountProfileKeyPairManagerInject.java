package xyz.bluspring.kilt.injects.client.multiplayer;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.mojang.authlib.minecraft.UserApiService;
import net.neoforged.fml.loading.FMLLoader;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.client.multiplayer.AccountProfileKeyPairManager;

@Mixin(AccountProfileKeyPairManager.class)
public abstract class AccountProfileKeyPairManagerInject {
    @Shadow @Final private UserApiService userApiService;

    @WrapWithCondition(method = "lambda$readOrFetchProfileKeyPair$0", at = @At(value = "INVOKE", target = "Lorg/slf4j/Logger;error(Ljava/lang/String;Ljava/lang/Throwable;)V", remap = false))
    private boolean kilt$checkOffline(Logger instance, String s, Throwable throwable) {
        // TODO: add a throw for if the user is cracked
        return FMLLoader.getCurrent().isProduction() || this.userApiService != UserApiService.OFFLINE;
    }
}
