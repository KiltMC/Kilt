package xyz.bluspring.kilt.injections.client.renderer.state;

import xyz.bluspring.kilt.util.KiltHelper;
import xyz.bluspring.kilt.workarounds.BaseRenderStateWorkaround;

import net.minecraft.core.Holder;
import net.minecraft.world.level.saveddata.maps.MapDecorationType;

public interface MapRenderStateInjection extends BaseRenderStateWorkaround {

    interface MapDecorationRenderStateInjection extends BaseRenderStateWorkaround {
        default Holder<MapDecorationType> kilt$getType() {
            throw KiltHelper.createMixinException(MapDecorationRenderStateInjection.class, "kilt$getType");
        }

        default void kilt$setType(Holder<MapDecorationType> type) {
            throw KiltHelper.createMixinException(MapDecorationRenderStateInjection.class, "kilt$setType");
        }
    }
}
