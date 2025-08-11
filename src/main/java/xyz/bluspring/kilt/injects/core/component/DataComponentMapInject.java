package xyz.bluspring.kilt.injects.core.component;

import net.minecraft.core.component.DataComponentMap;
import net.neoforged.neoforge.common.extensions.IDataComponentMapBuilderExtensions;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(DataComponentMap.class)
public abstract class DataComponentMapInject {
    @Mixin(DataComponentMap.Builder.class)
    public static abstract class BuilderInject implements IDataComponentMapBuilderExtensions {}
}
