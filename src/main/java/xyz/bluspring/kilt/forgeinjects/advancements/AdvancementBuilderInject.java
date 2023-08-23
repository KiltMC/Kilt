package xyz.bluspring.kilt.forgeinjects.advancements;

import com.google.gson.JsonObject;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraftforge.common.crafting.conditions.ICondition;
import net.minecraftforge.common.extensions.IForgeAdvancementBuilder;
import org.spongepowered.asm.mixin.Mixin;
import xyz.bluspring.kilt.helpers.mixin.CreateStatic;
import xyz.bluspring.kilt.injections.advancements.AdvancementBuilderInjection;

@Mixin(Advancement.Builder.class)
public class AdvancementBuilderInject implements IForgeAdvancementBuilder, AdvancementBuilderInjection {
    @CreateStatic
    private static Advancement.Builder fromJson(JsonObject json, DeserializationContext deserializationContext, ICondition.IContext context) {
        return AdvancementBuilderInjection.fromJson(json, deserializationContext, context);
    }
}
