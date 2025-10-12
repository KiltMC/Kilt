package xyz.bluspring.kilt.injects.client.gui.screens.worldselection;

import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.world.level.WorldDataConfiguration;
import net.neoforged.fml.ModLoader;
import net.neoforged.neoforge.event.AddPackFindersEvent;
import net.neoforged.neoforge.resource.ResourcePackLoader;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.nio.file.Path;

@Mixin(CreateWorldScreen.class)
public abstract class CreateWorldScreenInject {
    @Shadow
    @Nullable
    private PackRepository tempDataPackRepository;

    @Inject(method = "openFresh", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/worldselection/CreateWorldScreen;createDefaultLoadConfig(Lnet/minecraft/server/packs/repository/PackRepository;Lnet/minecraft/world/level/WorldDataConfiguration;)Lnet/minecraft/server/WorldLoader$InitConfig;"))
    private static void populatePackRepository(Minecraft minecraft, Screen lastScreen, CallbackInfo ci, @Local PackRepository packRepository) {
        ResourcePackLoader.populatePackRepository(packRepository, PackType.SERVER_DATA, false);
    }

    @Inject(method = "getDataPackSelectionSettings", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/packs/repository/PackRepository;reload()V"))
    private void populatePackRepository(WorldDataConfiguration worldDataConfiguration, CallbackInfoReturnable<Pair<Path, PackRepository>> cir) {
        ResourcePackLoader.populatePackRepository(this.tempDataPackRepository, net.minecraft.server.packs.PackType.SERVER_DATA, false);
    }

    // Kilt: handled by Fabric API
}
