package xyz.bluspring.kilt.injections.world.level.pathfinder;

import net.minecraft.world.level.pathfinder.BlockPathTypes;
import xyz.bluspring.kilt.mixin.BlockPathTypesAccessor;
import xyz.bluspring.kilt.util.EnumUtils;

public interface BlockPathTypesInjection {
    // wtf is malus
    static BlockPathTypes create(String name, float malus) {
        return EnumUtils.addEnumToClass(
                BlockPathTypes.class,
                BlockPathTypesAccessor.getValues(),
                name,
                (size) -> BlockPathTypesAccessor.createBlockPathTypes(name, size, malus),
                (values) -> BlockPathTypesAccessor.setValues(values.toArray(new BlockPathTypes[0]))
        );
    }

    default BlockPathTypes getDanger() {
        throw new IllegalStateException();
    }
}
