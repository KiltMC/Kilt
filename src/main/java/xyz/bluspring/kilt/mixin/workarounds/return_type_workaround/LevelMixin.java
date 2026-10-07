package xyz.bluspring.kilt.mixin.workarounds.return_type_workaround;

import java.util.Collection;

import net.neoforged.neoforge.entity.PartEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import xyz.bluspring.kilt.injections.world.level.LevelInjection;

import net.minecraft.world.level.Level;

@Mixin(Level.class)
public abstract class LevelMixin implements LevelInjection {
    // Kilt: funny compile time workaround
    @Unique
    public Collection<? extends PartEntity<?>> dragonParts() {
        return this.kilt$getPartEntities();
    }
}
