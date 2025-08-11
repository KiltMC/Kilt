package xyz.bluspring.kilt.injections.blaze3d.platform;

import com.mojang.blaze3d.platform.GlStateManager;
import net.neoforged.neoforge.client.GlStateBackup;
import org.spongepowered.asm.mixin.Unique;

public interface GlStateManagerInjection {
    @Unique
    static void _backupGlState(GlStateBackup state) {
        state.blendEnabled = GlStateManager.BLEND.mode.enabled;
        state.blendSrcRgb = GlStateManager.BLEND.srcRgb;
        state.blendDestRgb = GlStateManager.BLEND.dstRgb;
        state.blendSrcAlpha = GlStateManager.BLEND.srcAlpha;
        state.blendDestAlpha = GlStateManager.BLEND.dstAlpha;
        state.depthEnabled = GlStateManager.DEPTH.mode.enabled;
        state.depthMask = GlStateManager.DEPTH.mask;
        state.depthFunc = GlStateManager.DEPTH.func;
        state.cullEnabled = GlStateManager.CULL.enable.enabled;
        state.polyOffsetFillEnabled = GlStateManager.POLY_OFFSET.fill.enabled;
        state.polyOffsetLineEnabled = GlStateManager.POLY_OFFSET.line.enabled;
        state.polyOffsetFactor = GlStateManager.POLY_OFFSET.factor;
        state.polyOffsetUnits = GlStateManager.POLY_OFFSET.units;
        state.colorLogicEnabled = GlStateManager.COLOR_LOGIC.enable.enabled;
        state.colorLogicOp = GlStateManager.COLOR_LOGIC.op;
        state.stencilFuncFunc = GlStateManager.STENCIL.func.func;
        state.stencilFuncRef = GlStateManager.STENCIL.func.ref;
        state.stencilFuncMask = GlStateManager.STENCIL.func.mask;
        state.stencilMask = GlStateManager.STENCIL.mask;
        state.stencilFail = GlStateManager.STENCIL.fail;
        state.stencilZFail = GlStateManager.STENCIL.zfail;
        state.stencilZPass = GlStateManager.STENCIL.zpass;
        state.scissorEnabled = GlStateManager.SCISSOR.mode.enabled;
        state.colorMaskRed = GlStateManager.COLOR_MASK.red;
        state.colorMaskGreen = GlStateManager.COLOR_MASK.green;
        state.colorMaskBlue = GlStateManager.COLOR_MASK.blue;
        state.colorMaskAlpha = GlStateManager.COLOR_MASK.alpha;
    }

    @Unique
    static void _restoreGlState(GlStateBackup state) {
        GlStateManager.BLEND.mode.setEnabled(state.blendEnabled);
        GlStateManager._blendFuncSeparate(state.blendSrcRgb, state.blendDestRgb, state.blendSrcAlpha, state.blendDestAlpha);
        GlStateManager.DEPTH.mode.setEnabled(state.depthEnabled);
        GlStateManager._depthMask(state.depthMask);
        GlStateManager._depthFunc(state.depthFunc);
        GlStateManager.CULL.enable.setEnabled(state.cullEnabled);
        GlStateManager.POLY_OFFSET.fill.setEnabled(state.polyOffsetFillEnabled);
        GlStateManager.POLY_OFFSET.line.setEnabled(state.polyOffsetLineEnabled);
        GlStateManager._polygonOffset(state.polyOffsetFactor, state.polyOffsetUnits);
        GlStateManager.COLOR_LOGIC.enable.setEnabled(state.colorLogicEnabled);
        GlStateManager._logicOp(state.colorLogicOp);
        GlStateManager._stencilFunc(state.stencilFuncFunc, state.stencilFuncRef, state.stencilFuncMask);
        GlStateManager._stencilMask(state.stencilMask);
        GlStateManager._stencilOp(state.stencilFail, state.stencilZFail, state.stencilZPass);
        GlStateManager.SCISSOR.mode.setEnabled(state.scissorEnabled);
        GlStateManager._colorMask(state.colorMaskRed, state.colorMaskGreen, state.colorMaskBlue, state.colorMaskAlpha);
    }
}
