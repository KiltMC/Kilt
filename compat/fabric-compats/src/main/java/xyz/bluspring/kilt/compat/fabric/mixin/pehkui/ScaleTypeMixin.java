package xyz.bluspring.kilt.compat.fabric.mixin.pehkui;

import kotlin.Lazy;
import kotlin.LazyKt;
import net.fabricmc.fabric.api.event.Event;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import virtuoel.pehkui.api.ScaleEventCallback;
import virtuoel.pehkui.api.ScaleType;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Mixin(ScaleType.class)
public abstract class ScaleTypeMixin {
    @Shadow @Final private Event<ScaleEventCallback> scaleChangedEvent;
    @Shadow @Final private Event<ScaleEventCallback> preTickEvent;
    @Shadow @Final private Event<ScaleEventCallback> postTickEvent;

    public Collection<ScaleEventCallback> kilt$pehkui$getScaleChangedEvent() {
        return kilt$pehkui$createEventCollection(this.scaleChangedEvent);
    }

    public Collection<ScaleEventCallback> kilt$pehkui$getPreTickEvent() {
        return kilt$pehkui$createEventCollection(this.preTickEvent);
    }

    public Collection<ScaleEventCallback> kilt$pehkui$getPostTickEvent() {
        return kilt$pehkui$createEventCollection(this.postTickEvent);
    }

    @Unique private static final Class<?> ARRAY_BACKED_EVENT_CLASS;
    @Unique private static final Field HANDLERS;

    static {
        try {
            ARRAY_BACKED_EVENT_CLASS = Class.forName("net.fabricmc.fabric.impl.base.event.ArrayBackedEvent");
            HANDLERS = ARRAY_BACKED_EVENT_CLASS.getDeclaredField("handlers");
            HANDLERS.setAccessible(true);
        } catch (ClassNotFoundException | NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    @Unique
    private static <T> Collection<T> kilt$pehkui$createEventCollection(Event<T> event) {
        try {
            var handlers = (T[]) HANDLERS.get(event);
            return List.of(handlers);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
