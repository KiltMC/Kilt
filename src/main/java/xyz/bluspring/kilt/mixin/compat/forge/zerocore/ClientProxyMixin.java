package xyz.bluspring.kilt.mixin.compat.forge.zerocore;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;

@Pseudo
@Mixin(targets = "it.zerono.mods.zerocore.internal.proxy.ClientProxy")
public abstract class ClientProxyMixin {
    @WrapMethod(method = "markBlockRangeForRenderUpdate")
    private void kilt$callOnRenderThread(BlockPos min, BlockPos max, Operation<Void> original) {
        Minecraft.getInstance().execute(() -> original.call(min, max));
    }
}
