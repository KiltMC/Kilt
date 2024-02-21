package xyz.bluspring.kilt.injections.world.item;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraftforge.registries.IForgeRegistry;
import org.jetbrains.annotations.Nullable;
import xyz.bluspring.kilt.mixin.ItemDisplayContextAccessor;
import xyz.bluspring.kilt.util.EnumUtils;

public interface ItemDisplayContextInjection {
    // if the ID is > 127 start doing negative IDs
    IForgeRegistry.AddCallback<ItemDisplayContext> ADD_CALLBACK = (owner, stage, id, key, obj, oldObj) -> obj.id = id > Byte.MAX_VALUE ? (byte) - (id - Byte.MAX_VALUE) : (byte) id;

    static ItemDisplayContext create(String keyName, ResourceLocation serializedName, @Nullable ItemDisplayContext fallback) {
        return EnumUtils.addEnumToClass(
                ItemDisplayContext.class,
                ItemDisplayContextAccessor.getValues(),
                keyName,
                (size) -> {
                    var ctx = ItemDisplayContextAccessor.createItemDisplayContext(keyName, size, 0, serializedName.toString());
                    ((ItemDisplayContextInjection) (Object) ctx).kilt$markModded();
                    ((ItemDisplayContextInjection) (Object) ctx).kilt$setFallback(fallback);

                    return ctx;
                },
                (values) -> ItemDisplayContextAccessor.setValues(values.toArray(new ItemDisplayContext[0]))
        );
    }

    boolean isModded();
    @Nullable ItemDisplayContext fallback();

    void kilt$markModded();
    void kilt$setFallback(ItemDisplayContext fallback);
}
