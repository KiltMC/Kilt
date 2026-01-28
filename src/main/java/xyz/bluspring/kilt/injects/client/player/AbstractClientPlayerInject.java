package xyz.bluspring.kilt.injects.client.player;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.authlib.GameProfile;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.client.ClientHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(AbstractClientPlayer.class)
public abstract class AbstractClientPlayerInject extends Player {
    public AbstractClientPlayerInject(Level level, BlockPos pos, float yRot, GameProfile gameProfile) {
        super(level, pos, yRot, gameProfile);
    }

    // Kilt TODO: fix
//    @WrapOperation(method = "getFieldOfViewModifier", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Mth;lerp(FFF)F"))
//    private float kilt$getForgeFovModifier(float delta, float start, float end, Operation<Float> original) {
//        var modified = ClientHooks.getFieldOfViewModifier(this, end);
//        if (ClientHooks.kilt$isDefault.getAndSet(false)) {
//            return original.call(delta, start, end);
//        }
//
//        return modified;
//    }
}
