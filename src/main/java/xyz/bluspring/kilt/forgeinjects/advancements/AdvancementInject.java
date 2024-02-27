// TRACKED HASH: 2e8de1cfa5013ee685231175a216b94cd6918fad
package xyz.bluspring.kilt.forgeinjects.advancements;

import com.google.gson.JsonObject;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraftforge.common.crafting.conditions.ICondition;
import net.minecraftforge.common.extensions.IForgeAdvancementBuilder;
import org.spongepowered.asm.mixin.Mixin;
import xyz.bluspring.kilt.helpers.mixin.CreateStatic;
import xyz.bluspring.kilt.injections.advancements.AdvancementBuilderInjection;

public class AdvancementInject {
    @Mixin(Advancement.Builder.class)
    public static class BuilderInject implements IForgeAdvancementBuilder, AdvancementBuilderInjection {
        @CreateStatic
        private static Advancement.Builder fromJson(JsonObject json, DeserializationContext deserializationContext, ICondition.IContext context) {
            return AdvancementBuilderInjection.fromJson(json, deserializationContext, context);
        }
    }

}