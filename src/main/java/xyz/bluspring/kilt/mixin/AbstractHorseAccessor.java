package xyz.bluspring.kilt.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.animal.equine.AbstractHorse;

@Mixin(AbstractHorse.class)
public interface AbstractHorseAccessor {
    @Accessor
    SimpleContainer getInventory();
}
