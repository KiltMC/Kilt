package xyz.bluspring.kilt;

import xyz.bluspring.kilt.injections.world.level.saveddata.SavedDataTypeInjection;

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

    public record Quack<T>(SavedDataTypeInjection.Factory<T> test) {
        public Quack(Factory<T> test) {
            this((SavedDataTypeInjection.Factory<T>) test);
        }

        public interface Factory<T> extends SavedDataTypeInjection.Factory<T> {
        }
    }
}
