package xyz.bluspring.kilt.compat.fabric.mixin.cctweaked;

import dan200.computercraft.api.client.turtle.RegisterTurtleModellersEvent;
import dan200.computercraft.client.model.ExtraModels;
import dan200.computercraft.client.turtle.TurtleUpgradeModellers;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.neoforged.fml.ModLoader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collection;

@Mixin(ExtraModels.class)
public abstract class ExtraModelsMixin {
    @Inject(method = "loadAll", at = @At("HEAD"))
    private static void kilt$fireForgeEvent(ResourceManager resources, CallbackInfoReturnable<Collection<ResourceLocation>> cir) {
        ModLoader.postEvent(new RegisterTurtleModellersEvent(TurtleUpgradeModellers::register));
    }
}
