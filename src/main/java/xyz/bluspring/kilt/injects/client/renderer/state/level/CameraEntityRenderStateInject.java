package xyz.bluspring.kilt.injects.client.renderer.state.level;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import xyz.bluspring.kilt.injections.client.renderer.state.level.CameraEntityRenderStateInjection;

import net.minecraft.client.renderer.state.level.CameraEntityRenderState;

@Mixin(CameraEntityRenderState.class)
public abstract class CameraEntityRenderStateInject implements CameraEntityRenderStateInjection {
    @Unique public boolean preventFlinch;

    @Override
    public boolean kilt$preventFlinch() {
        return this.preventFlinch;
    }

    @Override
    public void kilt$preventFlinch(boolean value) {
        this.preventFlinch = value;
    }
}
