package xyz.bluspring.kilt.injects.world.entity;

import net.minecraft.world.entity.TamableAnimal;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(TamableAnimal.class)
public abstract class TamableAnimalInject {
    // Kilt: this feels like a bugfix, we're not fixing this unless it's actually critical.
}
