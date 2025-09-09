package xyz.bluspring.kilt.compat.fabric.mixin.geckolib;

import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;
import org.spongepowered.asm.mixin.Mixin;
import software.bernie.geckolib.event.GeoRenderEvent;
import xyz.bluspring.kilt.helpers.mixin.CreateInitializer;
import xyz.bluspring.kilt.helpers.mixin.Extends;

// Kilt: We have to do this because GeckoLib wasn't built as a multiloader mod until 1.20.6 and later.
//       This is a mess, I hate everything about this.
@Mixin(GeoRenderEvent.class)
public interface GeoRenderEventMixin {
    @Mixin(GeoRenderEvent.Armor.class)
    @Extends(Event.class)
    abstract class ArmorMixin {
        @Cancelable
        @Mixin(GeoRenderEvent.Armor.Pre.class)
        public abstract static class PreMixin extends GeoRenderEvent.Armor {
            @CreateInitializer
            public PreMixin() {
                super(null);
            }
        }

        @Mixin(GeoRenderEvent.Armor.Post.class)
        public abstract static class PostMixin extends GeoRenderEvent.Armor {
            @CreateInitializer
            public PostMixin() {
                super(null);
            }
        }

        @Mixin(GeoRenderEvent.Armor.CompileRenderLayers.class)
        public abstract static class CompileRenderLayersMixin extends GeoRenderEvent.Armor {
            @CreateInitializer
            public CompileRenderLayersMixin() {
                super(null);
            }
        }
    }

    @Mixin(GeoRenderEvent.Block.class)
    @Extends(Event.class)
    abstract class BlockMixin {
        @Cancelable
        @Mixin(GeoRenderEvent.Block.Pre.class)
        public abstract static class PreMixin extends GeoRenderEvent.Block {
            @CreateInitializer
            public PreMixin() {
                super(null);
            }
        }

        @Mixin(GeoRenderEvent.Block.Post.class)
        public abstract static class PostMixin extends GeoRenderEvent.Block {
            @CreateInitializer
            public PostMixin() {
                super(null);
            }
        }

        @Mixin(GeoRenderEvent.Block.CompileRenderLayers.class)
        public abstract static class CompileRenderLayersMixin extends GeoRenderEvent.Block {
            @CreateInitializer
            public CompileRenderLayersMixin() {
                super(null);
            }
        }
    }

    @Mixin(GeoRenderEvent.Entity.class)
    @Extends(Event.class)
    abstract class EntityMixin {
        @Cancelable
        @Mixin(GeoRenderEvent.Entity.Pre.class)
        public abstract static class PreMixin extends GeoRenderEvent.Entity {
            @CreateInitializer
            public PreMixin() {
                super(null);
            }
        }

        @Mixin(GeoRenderEvent.Entity.Post.class)
        public abstract static class PostMixin extends GeoRenderEvent.Entity {
            @CreateInitializer
            public PostMixin() {
                super(null);
            }
        }

        @Mixin(GeoRenderEvent.Entity.CompileRenderLayers.class)
        public abstract static class CompileRenderLayersMixin extends GeoRenderEvent.Entity {
            @CreateInitializer
            public CompileRenderLayersMixin() {
                super(null);
            }
        }
    }

    @Mixin(GeoRenderEvent.Item.class)
    @Extends(Event.class)
    abstract class ItemMixin {
        @Cancelable
        @Mixin(GeoRenderEvent.Item.Pre.class)
        public abstract static class PreMixin extends GeoRenderEvent.Item {
            @CreateInitializer
            public PreMixin() {
                super(null);
            }
        }

        @Mixin(GeoRenderEvent.Item.Post.class)
        public abstract static class PostMixin extends GeoRenderEvent.Item {
            @CreateInitializer
            public PostMixin() {
                super(null);
            }
        }

        @Mixin(GeoRenderEvent.Item.CompileRenderLayers.class)
        public abstract static class CompileRenderLayersMixin extends GeoRenderEvent.Item {
            @CreateInitializer
            public CompileRenderLayersMixin() {
                super(null);
            }
        }
    }

    @Mixin(GeoRenderEvent.Object.class)
    @Extends(Event.class)
    abstract class ObjectMixin {
        @Cancelable
        @Mixin(GeoRenderEvent.Object.Pre.class)
        public abstract static class PreMixin extends GeoRenderEvent.Object {
            @CreateInitializer
            public PreMixin() {
                super(null);
            }
        }

        @Mixin(GeoRenderEvent.Object.Post.class)
        public abstract static class PostMixin extends GeoRenderEvent.Object {
            @CreateInitializer
            public PostMixin() {
                super(null);
            }
        }

        @Mixin(GeoRenderEvent.Object.CompileRenderLayers.class)
        public abstract static class CompileRenderLayersMixin extends GeoRenderEvent.Object {
            @CreateInitializer
            public CompileRenderLayersMixin() {
                super(null);
            }
        }
    }

    @Mixin(GeoRenderEvent.ReplacedEntity.class)
    @Extends(Event.class)
    abstract class ReplacedEntityMixin {
        @Cancelable
        @Mixin(GeoRenderEvent.ReplacedEntity.Pre.class)
        public abstract static class PreMixin extends GeoRenderEvent.ReplacedEntity {
            @CreateInitializer
            public PreMixin() {
                super(null);
            }
        }

        @Mixin(GeoRenderEvent.ReplacedEntity.Post.class)
        public abstract static class PostMixin extends GeoRenderEvent.ReplacedEntity {
            @CreateInitializer
            public PostMixin() {
                super(null);
            }
        }

        @Mixin(GeoRenderEvent.ReplacedEntity.CompileRenderLayers.class)
        public abstract static class CompileRenderLayersMixin extends GeoRenderEvent.ReplacedEntity {
            @CreateInitializer
            public CompileRenderLayersMixin() {
                super(null);
            }
        }
    }
}
