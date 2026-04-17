package xyz.bluspring.kilt.injections.client.renderer.chunk;

import java.util.List;

import net.neoforged.neoforge.client.event.AddSectionGeometryEvent;
import xyz.bluspring.kilt.util.KiltHelper;

public interface SectionRenderDispatcherInjection {
    interface RenderSectionInjection {
        interface RebuildTaskInjection {
            default void kilt$setAdditionalRenderers(List<AddSectionGeometryEvent.AdditionalSectionRenderer> renderers) {
                throw KiltHelper.createMixinException(RebuildTaskInjection.class, "kilt$setAdditionalRenderers");
            }
        }
    }
}
