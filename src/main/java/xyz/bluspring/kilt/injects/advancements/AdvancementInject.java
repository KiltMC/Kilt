// TRACKED HASH: 2e8de1cfa5013ee685231175a216b94cd6918fad
package xyz.bluspring.kilt.injects.advancements;

import com.mojang.serialization.Codec;
import net.minecraft.advancements.Advancement;
import net.neoforged.neoforge.common.conditions.ConditionalOps;
import net.neoforged.neoforge.common.conditions.WithConditions;
import net.neoforged.neoforge.common.extensions.IAdvancementBuilderExtension;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import xyz.bluspring.kilt.helpers.mixin.CreateStatic;
import xyz.bluspring.kilt.injections.advancements.AdvancementBuilderInjection;

import java.util.Optional;

@Mixin(Advancement.class)
public class AdvancementInject {
    @Shadow @Final public static Codec<Advancement> CODEC;

    @CreateStatic
    private static final Codec<Optional<WithConditions<Advancement>>> CONDITIONAL_CODEC = ConditionalOps.createConditionalCodecWithConditions(CODEC);

    @Mixin(Advancement.Builder.class)
    public static class BuilderInject implements IAdvancementBuilderExtension, AdvancementBuilderInjection {
    }

}