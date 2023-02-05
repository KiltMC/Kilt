package xyz.bluspring.kilt.injections.entity;

import net.minecraft.world.entity.MobCategory;
import xyz.bluspring.kilt.mixin.MobCategoryAccessor;
import xyz.bluspring.kilt.util.EnumUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public interface MobCategoryInjection {
    Map<String, MobCategory> BY_NAME = new HashMap<>();

    static MobCategory byName(String name) {
        return BY_NAME.get(name);
    }

    static MobCategory create(String name, String id, int maxNumberOfCreatureIn, boolean isPeacefulCreatureIn, boolean isAnimalIn, int despawnDistance) {
        return EnumUtils.addEnumToClass(
                MobCategory.class,
                MobCategoryAccessor.getValues(),
                name, (size) -> MobCategoryAccessor.createMobCategory(name, size, id, maxNumberOfCreatureIn, isPeacefulCreatureIn, isAnimalIn, despawnDistance),
                (values) -> MobCategoryAccessor.setValues(values.toArray(new MobCategory[0]))
        );
    }
}
