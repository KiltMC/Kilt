package xyz.bluspring.kilt.injections.entity;

import net.minecraft.world.entity.MobCategory;

public interface MobCategoryInjection {
    static MobCategory byName(String name) {
        // TODO: Extend enum. Probably using Fabric-ASM?
        return MobCategory.valueOf(name);
    }
}
