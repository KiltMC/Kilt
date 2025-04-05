// TRACKED HASH: 0ce404d106018ee2fbf70e284692f9e57382cddd
package xyz.bluspring.kilt.forgeinjects.client.color.block;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.client.color.block.BlockColor;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.core.Holder;
import net.minecraft.core.IdMapper;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.registries.ForgeRegistries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.bluspring.kilt.injections.client.color.block.BlockColorsInjection;

import java.util.HashMap;
import java.util.Map;

@Mixin(BlockColors.class)
public class BlockColorsInject implements BlockColorsInjection {
    @Unique
    private Map<Holder.Reference<Block>, BlockColor> kilt$blockColors;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void kilt$createForgeBlockColorsWorkaround(CallbackInfo ci) {
        this.kilt$blockColors = new HashMap<>();
    }

    @Inject(at = @At("RETURN"), method = "createDefault")
    private static void kilt$initForgeBlockColors(CallbackInfoReturnable<BlockColors> cir) {
        ForgeHooksClient.onBlockColorsInit(cir.getReturnValue());
    }

    @Override
    public Map<Holder.Reference<Block>, BlockColor> kilt$getBlockColors() {
        return this.kilt$blockColors;
    }

    @WrapOperation(method = "getColor*", at = @At(value = "INVOKE", target = "Lnet/minecraft/core/IdMapper;byId(I)Ljava/lang/Object;"))
    private <T> T kilt$useForgeBlockColorIfPossible(IdMapper<T> instance, int id, Operation<T> original, @Local(argsOnly = true) BlockState state) {
        var delegate = ForgeRegistries.BLOCKS.getDelegate(state.getBlock());
        if (delegate.isPresent() && this.kilt$blockColors.containsKey(delegate.get())) {
            return (T) this.kilt$blockColors.get(delegate.get());
        }

        return original.call(instance, id);
    }

    @Inject(method = "register", at = @At(value = "INVOKE", target = "Lnet/minecraft/core/IdMapper;addMapping(Ljava/lang/Object;I)V"))
    private void kilt$registerBlockToForgeColor(BlockColor blockColor, Block[] blocks, CallbackInfo ci, @Local Block block) {
        var delegate = ForgeRegistries.BLOCKS.getDelegate(block);

        if (delegate.isPresent()) {
            this.kilt$blockColors.put(delegate.get(), blockColor);
        }
    }
}