package xyz.bluspring.kilt.forgeinjects.client.renderer.block.model;

import com.google.gson.Gson;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraftforge.client.model.ExtendedBlockModelDeserializer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(BlockModel.class)
public class BlockModelInject {
    @ModifyArg(method = "fromStream", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/GsonHelper;fromJson(Lcom/google/gson/Gson;Ljava/io/Reader;Ljava/lang/Class;)Ljava/lang/Object;"))
    private static Gson kilt$useForgeExtendedBlockModelDeserializer(Gson gson) {
        return ExtendedBlockModelDeserializer.INSTANCE;
    }
}
