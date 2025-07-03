package xyz.bluspring.kilt.injections.world.inventory;

import net.minecraft.world.item.alchemy.PotionBrewing;

public interface BrewingStandMenuInjection {
    interface PotionSlotInjection {
        void kilt$setPotionBrewing(PotionBrewing brewing);
    }
}
