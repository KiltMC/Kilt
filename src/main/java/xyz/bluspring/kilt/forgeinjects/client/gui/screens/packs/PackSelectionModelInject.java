package xyz.bluspring.kilt.forgeinjects.client.gui.screens.packs;

import net.minecraft.client.gui.screens.packs.PackSelectionModel;
import net.minecraft.server.packs.repository.Pack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import xyz.bluspring.kilt.injections.client.gui.screens.packs.PackSelectionModelEntryInjection;
import xyz.bluspring.kilt.injections.server.packs.repository.PackInjection;

@Mixin(PackSelectionModel.class)
public abstract class PackSelectionModelInject {
    @Mixin(PackSelectionModel.Entry.class)
    public interface EntryInject extends PackSelectionModelEntryInjection {
        @Override
        default boolean notHidden() {
            return true;
        }
    }

    @Mixin(targets = "net.minecraft.client.gui.screens.packs.PackSelectionModel$EntryBase")
    public abstract static class EntryBaseInject implements PackSelectionModelEntryInjection {
        @Shadow @Final private Pack pack;

        @Override
        public boolean notHidden() {
            return !((PackInjection) pack).isHidden();
        }
    }
}
