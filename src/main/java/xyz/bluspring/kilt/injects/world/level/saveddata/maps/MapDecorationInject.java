// TRACKED HASH: e813767a520dd65ca5a8c0efb62dcbfcd9dfde5e
package xyz.bluspring.kilt.injects.world.level.saveddata.maps;

import net.minecraft.world.level.saveddata.maps.MapDecoration;
import org.spongepowered.asm.mixin.Mixin;
import xyz.bluspring.kilt.injections.world.level.saveddata.maps.MapDecorationInjection;

@Mixin(MapDecoration.class)
public abstract class MapDecorationInject implements MapDecorationInjection {
    @Override
    public boolean render(int index) {
        return false;
    }
}