// TRACKED HASH: f1805acf0b005c3b2a599b6df75b8c44075821be
package xyz.bluspring.kilt.forgeinjects.server.packs.repository;

import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.repository.RepositorySource;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import xyz.bluspring.kilt.injections.server.PackRepositoryInjection;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Mixin(PackRepository.class)
public class PackRepositoryInject implements PackRepositoryInjection {
    @Shadow @Final @Mutable
    private Set<RepositorySource> sources;

    @Inject(at = @At("TAIL"), method = "<init>")
    public void kilt$makeSourcesMutable(RepositorySource[] sources, CallbackInfo ci) {
        this.sources = new LinkedHashSet<>(List.of(sources));
    }

    @Override
    public synchronized void addPackFinder(RepositorySource packFinder) {
        this.sources.add(packFinder);
    }
}