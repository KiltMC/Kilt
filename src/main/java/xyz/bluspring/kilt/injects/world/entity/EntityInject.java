package xyz.bluspring.kilt.injects.world.entity;

import net.neoforged.neoforge.common.extensions.IEntityExtension;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import xyz.bluspring.kilt.injections.world.entity.EntityInjection;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityFluidInteraction;

@Mixin(Entity.class)
public abstract class EntityInject implements EntityInjection, IEntityExtension {
    @Accessor("fluidInteraction")
    @Override
    public abstract EntityFluidInteraction getFluidInteraction();
}
