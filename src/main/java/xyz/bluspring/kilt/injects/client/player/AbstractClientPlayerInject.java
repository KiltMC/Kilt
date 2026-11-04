package xyz.bluspring.kilt.injects.client.player;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.authlib.GameProfile;
import net.neoforged.neoforge.client.ClientHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

@Mixin(AbstractClientPlayer.class)
public abstract class AbstractClientPlayerInject extends Player {
    public AbstractClientPlayerInject(Level level, BlockPos pos, float yRot, GameProfile gameProfile) {
        super(level, pos, yRot, gameProfile);
    }

    @WrapOperation(method = "getFieldOfViewModifier", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Mth;lerp(FFF)F"))
    private float kilt$getNeoForgeFovModifier(float delta, float start, float end, Operation<Float> original) {
        var event = ClientHooks.kilt$getFieldOfViewModifier(this, end);
        if (!event.kilt$wasModified) {
            return original.call(delta, start, end);
        }

        return event.getNewFovModifier();
    }
}
