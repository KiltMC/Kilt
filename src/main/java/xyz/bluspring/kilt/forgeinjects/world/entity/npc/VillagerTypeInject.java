package xyz.bluspring.kilt.forgeinjects.world.entity.npc;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.npc.VillagerType;
import net.minecraft.world.level.biome.Biome;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import xyz.bluspring.kilt.helpers.mixin.CreateStatic;

import java.util.Map;

@Mixin(VillagerType.class)
public abstract class VillagerTypeInject {
    @Shadow @Final public static Map<ResourceKey<Biome>, VillagerType> BY_BIOME;

    @CreateStatic
    private static void registerBiomeType(ResourceKey<Biome> biomeKey, VillagerType villagerType) {
        BY_BIOME.put(biomeKey, villagerType);
    }
}
