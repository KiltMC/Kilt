package xyz.bluspring.kilt.injections.world.entity;

import net.minecraft.world.entity.MobCategory;

import java.util.HashMap;
import java.util.Map;

public interface MobCategoryInjection {
    Map<String, MobCategory> BY_NAME = new HashMap<>();

    static MobCategory byName(String name) {
        return BY_NAME.get(name);
    }
}
