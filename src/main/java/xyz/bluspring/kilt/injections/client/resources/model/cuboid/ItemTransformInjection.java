package xyz.bluspring.kilt.injections.client.resources.model.cuboid;

import org.joml.Vector3fc;
import xyz.bluspring.kilt.helpers.ClientRecordMixinRefMaps;
import xyz.bluspring.kilt.util.KiltHelper;

import net.minecraft.client.resources.model.cuboid.ItemTransform;

public interface ItemTransformInjection {
    default Vector3fc rightRotation() {
        throw KiltHelper.createMixinException(ItemTransformInjection.class, "rightRotation");
    }

    static ItemTransform create(Vector3fc rotation, Vector3fc translation, Vector3fc scale, Vector3fc rightRotation) {
        var transform = new ItemTransform(rotation, translation, scale);
        ClientRecordMixinRefMaps.EXTENDED_ITEM_TRANSFORM.put(transform, new ClientRecordMixinRefMaps.ExtendedItemTransform(rightRotation));
        return transform;
    }
}
