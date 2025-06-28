package xyz.bluspring.kilt.forgeinjects.client.gui.screens.inventory;

import com.bawnorton.mixinsquared.TargetHandler;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraftforge.common.CreativeModeTabRegistry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.List;
import java.util.stream.Stream;

@Mixin(value = CreativeModeTabs.class, priority = 1009)
public class CreativeModeTabsInject {
//    Creative Tab sorting
//    @TargetHandler(
//            mixin = "net.fabricmc.fabric.mixin.itemgroup.ItemGroupsMixin",
//            name = "collect"
//    )
//    @Redirect(method = "@MixinSquared:Handler", at = @At(value = "INVOKE", target = "Ljava/util/stream/Stream;toList()Ljava/util/List;"))
//    private static List<ResourceKey<CreativeModeTab>> kilt$sortTabs(Stream<ResourceKey<CreativeModeTab>> stream) {
//        if (CreativeModeTabRegistry.getSortedCreativeModeTabs().isEmpty()) {
//            return stream.toList();
//        }
//        return CreativeModeTabRegistry.getSortedCreativeModeTabs().stream().map(creativeModeTab -> BuiltInRegistries.CREATIVE_MODE_TAB.getResourceKey(creativeModeTab).orElseThrow()).toList();
//    }
}
