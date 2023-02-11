package xyz.bluspring.kilt.injections.world.item;

import net.minecraft.world.item.CreativeModeTab;
import xyz.bluspring.kilt.remaps.world.item.CreativeModeTabRemap;

public interface CreativeModeTabInjection {
    static int getGroupCountSafe() {
        return CreativeModeTab.TABS.length;
    }

    static int updateIndex(int i) {
        return CreativeModeTabRemap.updateIndex(i);
    }
}
