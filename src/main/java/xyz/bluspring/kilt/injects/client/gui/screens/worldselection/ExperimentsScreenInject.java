package xyz.bluspring.kilt.injects.client.gui.screens.worldselection;

import java.util.function.Consumer;

import net.neoforged.neoforge.client.gui.ScrollableExperimentsScreen;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.worldselection.ExperimentsScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.world.flag.FeatureFlags;

@Mixin(ExperimentsScreen.class)
public abstract class ExperimentsScreenInject extends Screen {
    @Shadow @Final private Screen parent;
    @Shadow @Final private PackRepository packRepository;
    @Shadow @Final private Consumer<PackRepository> output;

    protected ExperimentsScreenInject(Component title) {
        super(title);
    }

    @Inject(method = "init", at = @At("HEAD"), cancellable = true)
    private void kilt$useScrollableExperimentsIfPossible(CallbackInfo ci) {
        if (FeatureFlags.REGISTRY.hasAnyModdedFlags()) {
            this.minecraft.setScreen(new ScrollableExperimentsScreen(this.parent, this.packRepository, this.output));
            ci.cancel();
        }
    }
}
