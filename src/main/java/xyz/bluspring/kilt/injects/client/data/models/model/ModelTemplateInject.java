package xyz.bluspring.kilt.injects.client.data.models.model;

import java.util.Map;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.neoforged.neoforge.client.model.generators.template.ExtendedModelTemplateBuilder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import xyz.bluspring.kilt.injections.client.data.models.model.ModelTemplateInjection;

import net.minecraft.client.data.models.model.ModelTemplate;
import net.minecraft.client.data.models.model.TextureSlot;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.resources.Identifier;

@Mixin(ModelTemplate.class)
public abstract class ModelTemplateInject implements ModelTemplateInjection {
    @Shadow
    protected abstract JsonElement lambda$create$0(Map<TextureSlot, Material> slots);

    @Override
    public JsonObject createBaseTemplate(Identifier target, Map<TextureSlot, Material> slots) {
        return this.lambda$create$0(slots).getAsJsonObject(); // no fucking shot, it can't be that easy
    }

    @Override
    public ExtendedModelTemplateBuilder extend() {
        return ExtendedModelTemplateBuilder.of((ModelTemplate) (Object) this);
    }
}
