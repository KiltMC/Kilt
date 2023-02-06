package xyz.bluspring.kilt;

import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ForgeRegistry;
import xyz.bluspring.kilt.injections.world.item.enchantment.EnchantmentCategoryInjection;

// I use this class to just throw stuff into and
// see how the bytecode looks when it's built.
// I know ASM-ifier exists, but this is a better
// learning experience for me.
public class TestingMoreShit {
    private static DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, "fuck");

    private static DeferredRegister<Block> BLOCKS2;

    private ForgeRegistry<Block> block;

    public TestingMoreShit(ForgeRegistry<Block> registry) {
        block = registry;
    }

    public boolean moreRandomShit(Item item) {
        return ((EnchantmentCategoryInjection) (Object) this).getDelegate().test(item);
    }

    static {
        try {
            BLOCKS2 = DeferredRegister.create((ForgeRegistry<Block>) Class.forName("net.minecraftforge.registries.ForgeRegistries").getDeclaredField("BLOCKS").get(null), "fuck");
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static class TestingDoubleTime extends TestingMoreShit {
        public TestingDoubleTime() {
            super(ForgeRegistries.BLOCKS);
        }
    }

    public static class TestingTripleTime extends TestingMoreShit {
        public TestingTripleTime() throws ClassNotFoundException, NoSuchFieldException, IllegalAccessException {
            super((ForgeRegistry<Block>) Class.forName("net.minecraftforge.registries.ForgeRegistries").getDeclaredField("BLOCKS").get(null));
        }
    }

    public enum MoreTestingBullshit {
        WELL, THEN;

        public static TestingDoubleTime quack() {
            return new TestingDoubleTime();
        }
    }
}
