package xyz.bluspring.kilt.forgeinjects.server.packs.repository;

import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.repository.RepositorySource;
import net.minecraftforge.event.AddPackFindersEvent;
import net.minecraftforge.fml.ModLoader;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.bluspring.kilt.injections.server.PackRepositoryInjection;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Mixin(PackRepository.class)
public class PackRepositoryInject implements PackRepositoryInjection {
    @Shadow @Final @Mutable
    private Set<RepositorySource> sources;

    @Inject(at = @At("TAIL"), method = "<init>(Lnet/minecraft/server/packs/repository/Pack$PackConstructor;[Lnet/minecraft/server/packs/repository/RepositorySource;)V")
    public void kilt$makeSourcesMutable(Pack.PackConstructor packConstructor, RepositorySource[] repositorySources, CallbackInfo ci) {
        this.sources = new HashSet<>(Arrays.asList(repositorySources));
    }

    @Inject(at = @At("TAIL"), method = "<init>(Lnet/minecraft/server/packs/PackType;[Lnet/minecraft/server/packs/repository/RepositorySource;)V")
    public void kilt$postPackFindersEvent(PackType packType, RepositorySource[] repositorySources, CallbackInfo ci) {
        ModLoader.get().postEvent(new AddPackFindersEvent(packType, this.sources::add));
    }

    @Override
    public synchronized void addPackFinder(RepositorySource packFinder) {
        this.sources.add(packFinder);
    }
}
