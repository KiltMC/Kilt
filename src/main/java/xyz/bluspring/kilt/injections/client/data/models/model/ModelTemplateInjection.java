package xyz.bluspring.kilt.injections.client.data.models.model;

import java.util.Map;

import com.google.gson.JsonObject;
import net.neoforged.neoforge.client.model.generators.template.ExtendedModelTemplateBuilder;
import xyz.bluspring.kilt.util.KiltHelper;

import net.minecraft.client.data.models.model.TextureSlot;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.resources.Identifier;

public interface ModelTemplateInjection {
    default JsonObject createBaseTemplate(Identifier target, Map<TextureSlot, Material> slots) {
        throw KiltHelper.createMixinException(ModelTemplateInjection.class, "createBaseTemplate");
    }

    default ExtendedModelTemplateBuilder extend() {
        throw KiltHelper.createMixinException(ModelTemplateInjection.class, "extend");
    }
}
