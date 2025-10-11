package xyz.bluspring.kilt;

import net.minecraft.world.level.biome.Biome;

// I use this class to just throw stuff into and
// see how the bytecode looks when it's built.
// I know ASM-ifier exists, but this is a better
// learning experience for me, and is definitely
// not because the ASM-ifier doesn't actually work
// for me.
public class TestingMoreShit {
    public static void test() {
        Biome.ClimateSettings settings = null;
        System.out.println(settings.downfall());
    }
}
