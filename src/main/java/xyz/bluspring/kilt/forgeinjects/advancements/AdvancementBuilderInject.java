package xyz.bluspring.kilt.forgeinjects.advancements;

import net.minecraft.advancements.Advancement;
import net.minecraftforge.common.extensions.IForgeAdvancementBuilder;
import org.spongepowered.asm.mixin.Mixin;
import xyz.bluspring.kilt.injections.advancements.AdvancementBuilderInjection;

@Mixin(Advancement.Builder.class)
public class AdvancementBuilderInject implements IForgeAdvancementBuilder, AdvancementBuilderInjection {
}
