package xyz.bluspring.kilt.forgeinjects.server.advancements;

import it.unimi.dsi.fastutil.Stack;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.advancements.Advancement;
import net.minecraft.server.advancements.AdvancementVisibilityEvaluator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import xyz.bluspring.kilt.helpers.mixin.CreateStatic;

import java.util.function.Predicate;

@Mixin(AdvancementVisibilityEvaluator.class)
public abstract class AdvancementVisibilityEvaluatorInject {
    @Shadow
    private static boolean evaluateVisibility(Advancement advancement, Stack<AdvancementVisibilityEvaluator.VisibilityRule> visibilityRules, Predicate<Advancement> predicate, AdvancementVisibilityEvaluator.Output output) {
        throw new IllegalStateException();
    }

    @CreateStatic
    private static boolean isVisible(Advancement advancement, Predicate<Advancement> test) {
        Stack<AdvancementVisibilityEvaluator.VisibilityRule> stack = new ObjectArrayList<>();

        for (int i = 0; i <= 2; ++i) {
            stack.push(AdvancementVisibilityEvaluator.VisibilityRule.NO_CHANGE);
        }

        return evaluateVisibility(advancement.getRoot(), stack, test, (a, b) -> {});
    }
}
