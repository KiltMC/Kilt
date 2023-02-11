package xyz.bluspring.kilt;

import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.PoweredRailBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ForgeRegistry;
import xyz.bluspring.kilt.injections.world.item.enchantment.EnchantmentCategoryInjection;
import xyz.bluspring.kilt.injections.world.level.block.PoweredRailBlockInjection;
import xyz.bluspring.kilt.mixin.EnchantmentCategoryAccessor;

import java.util.function.Supplier;

// I use this class to just throw stuff into and
// see how the bytecode looks when it's built.
// I know ASM-ifier exists, but this is a better
// learning experience for me, and is definitely
// not because the ASM-ifier doesn't actually work
// for me.
public class TestingMoreShit {
    private static DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, "fuck");

    private static DeferredRegister<Block> BLOCKS2;

    private ForgeRegistry<Block> block;

    public TestingMoreShit(ForgeRegistry<Block> registry) {
        block = registry;
    }

    public boolean moreRandomShit(Item item) {
        var quack = new Item(new Item.Properties().durability(0));
        //return ((EnchantmentCategoryInjection) (Object) this).getDelegate().test(item);
        return quack.equals("a");
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

    public static abstract class TestingQuadrupleTime extends PoweredRailBlock {
        public TestingQuadrupleTime(BlockBehaviour.Properties properties, boolean welp) {
            super(properties);
            ((PoweredRailBlockInjection) this).kilt$setActivator(welp);
        }

        public static void imdumb(boolean a) {
        }
    }

    public enum MoreTestingBullshit {
        WELL, THEN;

        public static TestingDoubleTime quack() {
            return new TestingDoubleTime();
        }

        public boolean wow(Item item) {
            return ((EnchantmentCategoryInjection) (Object) this).getDelegate() != null && ((EnchantmentCategoryInjection) (Object) this).getDelegate().test(item);
        }
    }
}
