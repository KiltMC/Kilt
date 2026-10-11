package xyz.bluspring.kilt.injects.core.component;

import net.neoforged.neoforge.common.extensions.IDataComponentMapBuilderExtensions;
import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.core.component.DataComponentMap;

@Mixin(DataComponentMap.class)
public interface DataComponentMapInject {
    @Mixin(DataComponentMap.Builder.class)
    abstract class BuilderInject implements IDataComponentMapBuilderExtensions {
        // Kilt: Already implemented by Fabric API
    }
}
