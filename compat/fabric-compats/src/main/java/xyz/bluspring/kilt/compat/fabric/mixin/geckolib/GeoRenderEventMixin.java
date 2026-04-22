package xyz.bluspring.kilt.compat.fabric.mixin.geckolib;

import com.moulberry.mixinconstraints.annotations.IfModLoaded;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;
import org.spongepowered.asm.mixin.Mixin;
import software.bernie.geckolib.event.GeoRenderEvent;
import xyz.bluspring.kilt.helpers.mixin.Extends;

@IfModLoaded("geckolib")
@Mixin(GeoRenderEvent.class)
public interface GeoRenderEventMixin {
    @Extends(Event.class)
    @Mixin(GeoRenderEvent.Armor.class)
    abstract class ArmorMixin {
        @Mixin(GeoRenderEvent.Armor.Pre.class)
        public static abstract class PreMixin implements ICancellableEvent {
        }
    }

    @Extends(Event.class)
    @Mixin(GeoRenderEvent.Block.class)
    abstract class BlockMixin {
        @Mixin(GeoRenderEvent.Block.Pre.class)
        public static abstract class PreMixin implements ICancellableEvent {
        }
    }

    @Extends(Event.class)
    @Mixin(GeoRenderEvent.Entity.class)
    abstract class EntityMixin {
        @Mixin(GeoRenderEvent.Entity.Pre.class)
        public static abstract class PreMixin implements ICancellableEvent {
        }
    }

    @Extends(Event.class)
    @Mixin(GeoRenderEvent.Item.class)
    abstract class ItemMixin {
        @Mixin(GeoRenderEvent.Item.Pre.class)
        public static abstract class PreMixin implements ICancellableEvent {
        }
    }

    @Extends(Event.class)
    @Mixin(GeoRenderEvent.Object.class)
    abstract class ObjectMixin {
        @Mixin(GeoRenderEvent.Object.Pre.class)
        public static abstract class PreMixin implements ICancellableEvent {
        }
    }

    @Extends(Event.class)
    @Mixin(GeoRenderEvent.ReplacedEntity.class)
    abstract class ReplacedEntityMixin {
        @Mixin(GeoRenderEvent.ReplacedEntity.Pre.class)
        public static abstract class PreMixin implements ICancellableEvent {
        }
    }
}
