package xyz.bluspring.kilt.injections.advancements;

import com.google.gson.JsonObject;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraftforge.common.crafting.ConditionalAdvancement;
import net.minecraftforge.common.crafting.conditions.ICondition;

public interface AdvancementBuilderInjection {
    static Advancement.Builder fromJson(JsonObject json, DeserializationContext deserializationContext, ICondition.IContext context) {
        var processedConditional = ConditionalAdvancement.processConditional(json, context);
        if (processedConditional == null)
            return null;

        return Advancement.Builder.fromJson(processedConditional, deserializationContext);
    }
}
