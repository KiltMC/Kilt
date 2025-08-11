package xyz.bluspring.kilt.injects.world.item;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.FireworkRocketItem;
import net.neoforged.neoforge.common.IExtensibleEnum;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import xyz.bluspring.kilt.helpers.mixin.CreateStatic;
import xyz.bluspring.kilt.injections.world.item.FireworkRocketItemShapeInjection;

@Mixin(FireworkRocketItem.class)
public abstract class FireworkRocketItemInject {

    @Mixin(FireworkRocketItem.Shape.class)
    public abstract static class ShapeInject implements IExtensibleEnum, FireworkRocketItemShapeInjection {
        @Shadow public abstract int getId();

        @Override
        public void save(CompoundTag tag) {
            tag.putByte("Type", (byte) this.getId());
            tag.putString("forge:shape_type", ((FireworkRocketItem.Shape) (Object) this).name());
        }

        @CreateStatic
        private static FireworkRocketItem.Shape getShape(CompoundTag tag) {
            return FireworkRocketItemShapeInjection.getShape(tag);
        }

        @CreateStatic
        private static FireworkRocketItem.Shape create(String registryName, int id, String shapeName) {
            return FireworkRocketItemShapeInjection.create(registryName, id, shapeName);
        }
    }
}
