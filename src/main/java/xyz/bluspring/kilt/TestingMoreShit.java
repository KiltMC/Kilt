package xyz.bluspring.kilt;

import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ForgeRegistry;

public class TestingMoreShit {
    private static DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, "fuck");

    private static DeferredRegister<Block> BLOCKS2;

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
}
