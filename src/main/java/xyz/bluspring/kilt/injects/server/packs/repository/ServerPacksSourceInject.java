// TRACKED HASH: c237d5894375964b673637a82cd2aa492a0db53f
package xyz.bluspring.kilt.injects.server.packs.repository;

import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.repository.ServerPacksSource;
import net.minecraft.world.level.validation.DirectoryValidator;
import net.neoforged.neoforge.event.AddPackFindersEvent;
import net.neoforged.fml.ModLoader;
import net.neoforged.neoforge.resource.ResourcePackLoader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.nio.file.Path;

@Mixin(value = ServerPacksSource.class, priority = 900)
public class ServerPacksSourceInject {
    @Inject(method = "createPackRepository(Ljava/nio/file/Path;Lnet/minecraft/world/level/validation/DirectoryValidator;)Lnet/minecraft/server/packs/repository/PackRepository;", at = @At("RETURN"))
    private static void kilt$registerPackFinders(Path folder, DirectoryValidator validator, CallbackInfoReturnable<PackRepository> cir) {
        ResourcePackLoader.populatePackRepository(cir.getReturnValue(), PackType.SERVER_DATA, false);
    }

    @Inject(method = "createVanillaTrustedRepository", at = @At("RETURN"))
    private static void kilt$registerPackFinders(CallbackInfoReturnable<PackRepository> cir) {
        ResourcePackLoader.populatePackRepository(cir.getReturnValue(), PackType.SERVER_DATA, true);
    }
}