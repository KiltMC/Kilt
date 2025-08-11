package xyz.bluspring.kilt.injects.client.gui.screens.inventory;

import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.neoforge.common.CreativeModeTabRegistry;
import org.spongepowered.asm.mixin.Mixin;

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
