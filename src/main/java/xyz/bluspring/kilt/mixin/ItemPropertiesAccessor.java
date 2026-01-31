package xyz.bluspring.kilt.mixin;

import com.google.common.collect.Interner;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Item.Properties.class)
public interface ItemPropertiesAccessor {
    @Accessor("COMPONENT_INTERNER")
    static Interner<DataComponentMap> getComponentInterner() {
        throw new UnsupportedOperationException();
    }

    @Invoker
    DataComponentMap callBuildAndValidateComponents();
}
