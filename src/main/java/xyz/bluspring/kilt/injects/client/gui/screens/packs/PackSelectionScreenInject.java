package xyz.bluspring.kilt.injects.client.gui.screens.packs;

import com.llamalad7.mixinextras.injector.ModifyReceiver;
import net.minecraft.client.gui.screens.packs.PackSelectionScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import xyz.bluspring.kilt.injections.client.gui.screens.packs.PackSelectionModelEntryInjection;

import java.util.function.Consumer;
import java.util.stream.Stream;

@Mixin(PackSelectionScreen.class)
public abstract class PackSelectionScreenInject {
    @ModifyReceiver(method = "updateList", at = @At(value = "INVOKE", target = "Ljava/util/stream/Stream;forEach(Ljava/util/function/Consumer;)V"))
    private <T> Stream<? extends T> kilt$filterByNotHidden(Stream<? extends T> instance, Consumer<? super T> consumer) {
        return instance.filter(e -> ((PackSelectionModelEntryInjection) e).notHidden());
    }
}
