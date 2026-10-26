package xyz.bluspring.kilt.injections.client.renderer;

import java.util.function.Consumer;

import net.neoforged.neoforge.client.IRenderableSection;
import xyz.bluspring.kilt.util.KiltHelper;

import net.minecraft.world.level.block.entity.BlockEntity;

public interface LevelRendererInjection {
    default int getTicks() {
        throw KiltHelper.createMixinException(LevelRendererInjection.class, "getTicks");
    }

    default void iterateVisibleBlockEntities(Consumer<BlockEntity> blockEntityConsumer) {
        throw KiltHelper.createMixinException(LevelRendererInjection.class, "iterateVisibleBlockEntities");
    }

    default Iterable<? extends IRenderableSection> getRenderableSections() {
        throw KiltHelper.createMixinException(LevelRendererInjection.class, "getRenderableSections");
    }
}
