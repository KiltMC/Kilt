package xyz.bluspring.kilt.injects.world.item;

import com.google.common.base.Suppliers;
import net.minecraft.world.item.ItemDisplayContext;
import net.neoforged.fml.common.asm.enumextension.*;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import xyz.bluspring.kilt.helpers.mixin.CreateInitializer;
import xyz.bluspring.kilt.helpers.mixin.CreateStatic;
import xyz.bluspring.kilt.injections.world.item.ItemDisplayContextInjection;

import java.util.function.Supplier;

@IndexedEnum
@NamedEnum(1)
@NetworkedEnum(NetworkedEnum.NetworkCheck.CLIENTBOUND)
@Mixin(ItemDisplayContext.class)
public abstract class ItemDisplayContextInject implements IExtensibleEnum, ItemDisplayContextInjection {
    @Unique private boolean isModded = false;
    @Unique private Supplier<ItemDisplayContext> fallback = () -> null;

    @ReservedConstructor
    private ItemDisplayContextInject(int id, String name) {
    }

    @CreateInitializer
    private ItemDisplayContextInject(int id, String name, @Nullable String fallbackName) {
        this(id, name);
        this.isModded = true;
        this.fallback = fallbackName == null ? () -> null : Suppliers.memoize(() -> ItemDisplayContext.valueOf(fallbackName));
    }

    @Override
    public boolean isModded() {
        return this.isModded;
    }

    @Override
    public @Nullable ItemDisplayContext fallback() {
        return this.fallback.get();
    }

    @CreateStatic
    private static ExtensionInfo getExtensionInfo() {
        return ExtensionInfo.nonExtended(ItemDisplayContext.class);
    }
}
