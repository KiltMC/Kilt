package xyz.bluspring.kilt.injects.client.renderer.state;

import net.neoforged.neoforge.client.renderstate.BaseRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import xyz.bluspring.kilt.helpers.mixin.Extends;
import xyz.bluspring.kilt.injections.client.renderer.state.MapRenderStateInjection;

import net.minecraft.client.renderer.state.MapRenderState;
import net.minecraft.core.Holder;
import net.minecraft.world.level.saveddata.maps.MapDecorationType;

@Mixin(MapRenderState.class)
@Extends(BaseRenderState.class)
public abstract class MapRenderStateInject {
    @Mixin(MapRenderState.MapDecorationRenderState.class)
    @Extends(BaseRenderState.class)
    public abstract static class MapDecorationRenderStateInject implements MapRenderStateInjection.MapDecorationRenderStateInjection {
        @Unique public Holder<MapDecorationType> type;

        @Override
        public Holder<MapDecorationType> kilt$getType() {
            return this.type;
        }

        @Override
        public void kilt$setType(Holder<MapDecorationType> type) {
            this.type = type;
        }
    }
}
