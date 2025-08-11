package xyz.bluspring.kilt.injects.server.packs.metadata.pack;

import com.google.gson.JsonObject;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.server.packs.metadata.pack.PackMetadataSection;
import net.minecraft.server.packs.metadata.pack.PackMetadataSectionSerializer;
import net.neoforged.neoforge.common.CommonHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import xyz.bluspring.kilt.injections.server.packs.metadata.pack.PackMetadataSectionInjection;

@Mixin(PackMetadataSectionSerializer.class)
public abstract class PackMetadataSectionSerializerInject {
    @ModifyReturnValue(method = "fromJson(Lcom/google/gson/JsonObject;)Lnet/minecraft/server/packs/metadata/pack/PackMetadataSection;", at = @At("RETURN"))
    private PackMetadataSection kilt$setTypedPackFormats(PackMetadataSection original, @Local(argsOnly = true) JsonObject jsonObject) {
        if (original != null) {
            ((PackMetadataSectionInjection) original).kilt$setPackTypeVersions(CommonHooks.readTypedPackFormats(jsonObject));
        }

        return original;
    }

    @ModifyReturnValue(method = "toJson(Lnet/minecraft/server/packs/metadata/pack/PackMetadataSection;)Lcom/google/gson/JsonObject;", at = @At("RETURN"))
    private JsonObject kilt$storeTypedPackFormats(JsonObject original, @Local(argsOnly = true) PackMetadataSection section) {
        CommonHooks.writeTypedPackFormats(original, section);
        return original;
    }
}
