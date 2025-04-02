package xyz.bluspring.kilt.injections.world.level.saveddata.maps;

import io.github.fabricators_of_create.porting_lib.extensions.extensions.MapDecorationExtensions;

public interface MapDecorationInjection extends MapDecorationExtensions {
    boolean render(int index);
}
