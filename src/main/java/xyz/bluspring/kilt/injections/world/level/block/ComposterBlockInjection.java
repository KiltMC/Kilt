package xyz.bluspring.kilt.injections.world.level.block;

import net.neoforged.neoforge.registries.datamaps.builtin.NeoForgeDataMaps;

import net.minecraft.world.item.ItemStack;

public interface ComposterBlockInjection {
    static float getValue(ItemStack item) {
        var value = item.getData(NeoForgeDataMaps.COMPOSTABLES);
        if (value != null)
            return value.chance();

        return -1;
    }
}
