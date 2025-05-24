package xyz.bluspring.kilt.forgeinjects.data.tags;

import net.minecraft.data.tags.GameEventTagsProvider;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(GameEventTagsProvider.class)
public abstract class GameEventTagsProviderInject {
    // Kilt: we have no reason to implement this
}
