package xyz.bluspring.kilt.mixin.porting_lib;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.github.fabricators_of_create.porting_lib.data.ConditionalRecipe;
import net.minecraft.util.GsonHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = ConditionalRecipe.Serializer.class, remap = false)
public class ConditionalRecipeSerializerMixin {
    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/util/GsonHelper;getAsJsonArray(Lcom/google/gson/JsonObject;Ljava/lang/String;)Lcom/google/gson/JsonArray;"), method = "fromJson", remap = false)
    public JsonArray kilt$processForgeAndFabricConditions(JsonObject jsonObject, String string) {
        // Process forge:conditional instead
        if (!jsonObject.has(string)) {
            GsonHelper.getAsJsonArray(jsonObject, "forge:conditional");
        }

        return GsonHelper.getAsJsonArray(jsonObject, string);
    }
}
