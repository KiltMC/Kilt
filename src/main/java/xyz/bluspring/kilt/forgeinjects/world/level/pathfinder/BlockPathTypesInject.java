package xyz.bluspring.kilt.forgeinjects.world.level.pathfinder;

import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraftforge.common.IExtensibleEnum;
import org.spongepowered.asm.mixin.Mixin;
import xyz.bluspring.kilt.helpers.mixin.CreateStatic;
import xyz.bluspring.kilt.injections.world.level.pathfinder.BlockPathTypesInjection;

@Mixin(BlockPathTypes.class)
public class BlockPathTypesInject implements BlockPathTypesInjection, IExtensibleEnum {
    @CreateStatic
    private static BlockPathTypes create(String name, float malus) {
        return BlockPathTypesInjection.create(name, malus);
    }

    @Override
    public BlockPathTypes getDanger() {
        var type = ((BlockPathTypes) (Object) this);

        return (type == BlockPathTypes.DAMAGE_FIRE || type == BlockPathTypes.DANGER_FIRE) ? BlockPathTypes.DANGER_FIRE
                : (type == BlockPathTypes.DAMAGE_CACTUS || type == BlockPathTypes.DANGER_CACTUS) ? BlockPathTypes.DANGER_CACTUS
                : (type == BlockPathTypes.DAMAGE_OTHER || type == BlockPathTypes.DANGER_OTHER) ? BlockPathTypes.DANGER_OTHER
                : (type == BlockPathTypes.LAVA) ? BlockPathTypes.DAMAGE_FIRE
                : null;
    }
}
