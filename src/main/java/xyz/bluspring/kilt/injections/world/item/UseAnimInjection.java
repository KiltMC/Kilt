package xyz.bluspring.kilt.injections.world.item;

import net.minecraft.world.item.UseAnim;
import xyz.bluspring.kilt.mixin.world.item.UseAnimAccessor;
import xyz.bluspring.kilt.util.EnumUtils;

public interface UseAnimInjection {
    UseAnim CUSTOM = create("CUSTOM");

    static UseAnim create(String name) {
        return EnumUtils.addEnumToClass(UseAnim.class, UseAnimAccessor.getValues(), name,
            size -> UseAnimAccessor.createUseAnim(name, size),
            values -> UseAnimAccessor.setValues(values.toArray(new UseAnim[0]))
        );
    }
}
