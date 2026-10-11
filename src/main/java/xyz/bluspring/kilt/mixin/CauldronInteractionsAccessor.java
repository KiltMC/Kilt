package xyz.bluspring.kilt.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.core.cauldron.CauldronInteraction;
import net.minecraft.core.cauldron.CauldronInteractions;
import net.minecraft.util.ExtraCodecs;

@Mixin(CauldronInteractions.class)
public interface CauldronInteractionsAccessor {
    @Accessor("ID_MAPPER")
    static ExtraCodecs.LateBoundIdMapper<String, CauldronInteraction.Dispatcher> kilt$getIdMapper() {
        throw new UnsupportedOperationException();
    }
}
