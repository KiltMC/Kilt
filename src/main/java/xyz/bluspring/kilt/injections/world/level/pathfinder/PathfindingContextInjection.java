package xyz.bluspring.kilt.injections.world.level.pathfinder;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.pathfinder.PathfindingContext;
import xyz.bluspring.kilt.processor.FabricInjectedInterface;
import xyz.bluspring.kilt.util.KiltHelper;

@FabricInjectedInterface(PathfindingContext.class)
public interface PathfindingContextInjection {
    default BlockPos currentEvalPos() {
        throw KiltHelper.createMixinException(PathfindingContextInjection.class, "currentEvalPos");
    }
}
