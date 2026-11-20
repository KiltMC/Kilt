package xyz.bluspring.kilt.injects.world.entity;

import net.neoforged.neoforge.attachment.AttachmentHolder;
import net.neoforged.neoforge.common.extensions.IEntityExtension;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.gen.Accessor;
import xyz.bluspring.kilt.helpers.mixin.Extends;
import xyz.bluspring.kilt.injections.world.entity.EntityInjection;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityFluidInteraction;

@Extends(AttachmentHolder.class)
@Mixin(Entity.class)
public abstract class EntityInject implements EntityInjection, IEntityExtension {
    @Shadow
    protected abstract void unsetRemoved();

    @Accessor("fluidInteraction")
    @Override
    public abstract EntityFluidInteraction getFluidInteraction();

    @Unique private boolean isAddedToLevel;

    @Override
    public boolean isAddedToLevel() {
        return this.isAddedToLevel;
    }

    @Override
    public void onAddedToLevel() {
        this.isAddedToLevel = true;
    }

    @Override
    public void onRemovedFromLevel() {
        this.isAddedToLevel = false;
    }

    @Override
    public void revive() {
        this.unsetRemoved();
    }
}
