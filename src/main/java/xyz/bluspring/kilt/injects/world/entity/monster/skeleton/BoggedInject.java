package xyz.bluspring.kilt.injects.world.entity.monster.skeleton;

import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.skeleton.AbstractSkeleton;
import net.minecraft.world.entity.monster.skeleton.Bogged;
import net.minecraft.world.level.Level;

@Mixin(Bogged.class)
public abstract class BoggedInject extends AbstractSkeleton {
    protected BoggedInject(EntityType<? extends AbstractSkeleton> entityType, Level level) {
        super(entityType, level);
    }

    // Kilt: shearing stuff
}
