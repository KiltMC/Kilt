package xyz.bluspring.kilt.injections.advancements;

import com.mojang.serialization.Codec;
import net.minecraft.advancements.Advancement;
import net.neoforged.neoforge.common.conditions.ConditionalOps;
import net.neoforged.neoforge.common.conditions.WithConditions;

import java.util.Optional;

public interface AdvancementInjection {
    Codec<Optional<WithConditions<Advancement>>> CONDITIONAL_CODEC = ConditionalOps.createConditionalCodecWithConditions(Advancement.CODEC);
}
