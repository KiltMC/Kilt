package xyz.bluspring.kilt.compat.neoconfig.mixin;

import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Function;

import com.moulberry.mixinconstraints.annotations.IfModLoaded;
import fuzs.forgeconfigapiport.fabric.impl.core.ForgeConfigRegistryImpl;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.config.IConfigSpec;
import net.neoforged.fml.config.LoadedConfig;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.config.ModConfigEvent;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@IfModLoaded("forgeconfigapiport")
@Mixin(ModConfig.class)
public abstract class ModConfigMixin {
    // Kilt: I don't... i..... FCAP why do you do this?!
    @Unique @ApiStatus.Internal @Nullable
    public net.minecraftforge.fml.config.ModConfig modConfig;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void kilt$neoconfig$adaptForgeConfigInstance(ModConfig.Type type, IConfigSpec spec, ModContainer container, String fileName, ReentrantLock lock, CallbackInfo ci) {
        this.modConfig = ForgeConfigRegistryImpl.adapt((ModConfig) (Object) this);
    }

    @Inject(method = "setConfig", at = @At(value = "INVOKE", target = "Lnet/neoforged/fml/ModContainer;acceptEvent(Lnet/neoforged/bus/api/Event;)V"))
    private void kilt$neoconfig$changeLoadedConfigForge(@Nullable LoadedConfig loadedConfig, Function<ModConfig, ModConfigEvent> eventConstructor, CallbackInfo ci) {
        if (this.modConfig != null)
            this.modConfig.loadedConfig = loadedConfig;
    }
}
