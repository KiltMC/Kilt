package xyz.bluspring.kilt.injections.world.item;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.FireworkRocketItem;
import xyz.bluspring.kilt.mixin.world.item.FireworkRocketItemShapeAccessor;
import xyz.bluspring.kilt.util.EnumUtils;

public interface FireworkRocketItemShapeInjection {
    void save(CompoundTag tag);

    static FireworkRocketItem.Shape getShape(CompoundTag tag) {
        var name = tag.contains("forge:shape_type", Tag.TAG_STRING) ? tag.getString("forge:shape_type") : null;
        if (name == null)
            return FireworkRocketItem.Shape.byId(tag.getByte("Type"));

        try {
            return FireworkRocketItem.Shape.valueOf(name);
        } catch (IllegalArgumentException ignored) {
            return FireworkRocketItem.Shape.SMALL_BALL;
        }
    }

    static FireworkRocketItem.Shape create(String registryName, int id, String shapeName) {
        return EnumUtils.addEnumToClass(
            FireworkRocketItem.Shape.class, FireworkRocketItemShapeAccessor.getValues(),
            registryName,
            (size) -> FireworkRocketItemShapeAccessor.createShape(registryName, size, id, shapeName),
            (values) -> FireworkRocketItemShapeAccessor.setValues(values.toArray(new FireworkRocketItem.Shape[0]))
        );
    }
}
