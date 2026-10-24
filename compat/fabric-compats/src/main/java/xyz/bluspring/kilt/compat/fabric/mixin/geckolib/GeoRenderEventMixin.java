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
    @IfModLoaded("geckolib")
    @Mixin(GeoRenderEvent.Armor.class)
    abstract class ArmorMixin {
        @IfModLoaded("geckolib")
        @Mixin(GeoRenderEvent.Armor.Pre.class)
        public static abstract class PreMixin implements ICancellableEvent {
        }
    }

    @Extends(Event.class)
    @IfModLoaded("geckolib")
    @Mixin(GeoRenderEvent.Block.class)
    abstract class BlockMixin {
        @IfModLoaded("geckolib")
        @Mixin(GeoRenderEvent.Block.Pre.class)
        public static abstract class PreMixin implements ICancellableEvent {
        }
    }

    @Extends(Event.class)
    @IfModLoaded("geckolib")
    @Mixin(GeoRenderEvent.Entity.class)
    abstract class EntityMixin {
        @IfModLoaded("geckolib")
        @Mixin(GeoRenderEvent.Entity.Pre.class)
        public static abstract class PreMixin implements ICancellableEvent {
        }
    }

    @Extends(Event.class)
    @IfModLoaded("geckolib")
    @Mixin(GeoRenderEvent.Item.class)
    abstract class ItemMixin {
        @IfModLoaded("geckolib")
        @Mixin(GeoRenderEvent.Item.Pre.class)
        public static abstract class PreMixin implements ICancellableEvent {
        }
    }

    @Extends(Event.class)
    @IfModLoaded("geckolib")
    @Mixin(GeoRenderEvent.Object.class)
    abstract class ObjectMixin {
        @IfModLoaded("geckolib")
        @Mixin(GeoRenderEvent.Object.Pre.class)
        public static abstract class PreMixin implements ICancellableEvent {
        }
    }

    @Extends(Event.class)
    @IfModLoaded("geckolib")
    @Mixin(GeoRenderEvent.ReplacedEntity.class)
    abstract class ReplacedEntityMixin {
        @IfModLoaded("geckolib")
        @Mixin(GeoRenderEvent.ReplacedEntity.Pre.class)
        public static abstract class PreMixin implements ICancellableEvent {
        }
    }
}
