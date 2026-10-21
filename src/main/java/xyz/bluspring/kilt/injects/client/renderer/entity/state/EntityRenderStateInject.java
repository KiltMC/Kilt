package xyz.bluspring.kilt.injects.client.renderer.entity.state;

import net.neoforged.neoforge.client.renderstate.BaseRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import xyz.bluspring.kilt.helpers.mixin.Extends;
import xyz.bluspring.kilt.injections.client.renderer.entity.state.EntityRenderStateInjection;

import net.minecraft.client.renderer.entity.state.EntityRenderState;

@Mixin(EntityRenderState.class)
@Extends(BaseRenderState.class)
public abstract class EntityRenderStateInject implements EntityRenderStateInjection {
    @Unique public float partialTick;

    @Override
    public float kilt$partialTick() {
        return this.partialTick;
    }

    @Override
    public void kilt$partialTick(float pt) {
        this.partialTick = pt;
    }
}
