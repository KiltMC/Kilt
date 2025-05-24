package xyz.bluspring.kilt.forgeinjects.tags;

import net.minecraft.tags.TagManager;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(TagManager.class)
public abstract class TagManagerInject {
    // Kilt: Handled by Fabric API
}
