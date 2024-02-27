// TRACKED HASH: c237d5894375964b673637a82cd2aa492a0db53f
package xyz.bluspring.kilt.forgeinjects.server.packs.repository;

import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.repository.ServerPacksSource;
import net.minecraftforge.event.AddPackFindersEvent;
import net.minecraftforge.fml.ModLoader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.nio.file.Path;

@Mixin(value = ServerPacksSource.class, priority = 900)
public class ServerPacksSourceInject {
    @Inject(method = "createPackRepository(Ljava/nio/file/Path;)Lnet/minecraft/server/packs/repository/PackRepository;", at = @At("RETURN"), cancellable = true)
    private static void kilt$registerPackFinders(Path path, CallbackInfoReturnable<PackRepository> cir) {
        var repository = cir.getReturnValue();
        ModLoader.get().postEvent(new AddPackFindersEvent(PackType.SERVER_DATA, repository::addPackFinder));
    }
}