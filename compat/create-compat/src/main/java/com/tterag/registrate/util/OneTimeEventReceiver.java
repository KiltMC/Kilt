package com.tterag.registrate.util;

import com.tterrag.registrate.AbstractRegistrate;
import com.tterrag.registrate.util.nullness.NonnullType;
import net.minecraftforge.common.MinecraftForge;
import net.neoforged.bus.EventBus;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.EventListenerHelper;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.IModBusEvent;
import net.neoforged.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.neoforged.fml.util.ObfuscationReflectionHelper;
import org.apache.commons.lang3.tuple.Triple;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import xyz.bluspring.kilt.compat.create.extensions.AbstractRegistrateForgeExtension;

import javax.annotation.Nullable;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public class OneTimeEventReceiver<T extends Event> implements Consumer<@NonnullType T> {
    private static final Logger log = LoggerFactory.getLogger("Registrate (Kilt)");

    public static <T extends Event & IModBusEvent> void addModListener(AbstractRegistrate<?> owner, Class<? super T> evtClass, Consumer<? super T> listener) {
        OneTimeEventReceiver.<T>addModListener(owner, EventPriority.NORMAL, evtClass, listener);
    }

    public static <T extends Event & IModBusEvent> void addModListener(AbstractRegistrate<?> owner, EventPriority priority, Class<? super T> evtClass, Consumer<? super T> listener) {
        if (!seenModBus) {
            seenModBus = true;
            addModListener(owner, FMLLoadCompleteEvent.class, OneTimeEventReceiver::onLoadComplete);
        }
        OneTimeEventReceiver.<T>addListener(((AbstractRegistrateForgeExtension<?>) owner).getModEventBus(), priority, evtClass, listener);
    }

    public static <T extends Event> void addForgeListener(Class<? super T> evtClass, Consumer<? super T> listener) {
        OneTimeEventReceiver.<T>addForgeListener(EventPriority.NORMAL, evtClass, listener);
    }

    public static <T extends Event> void addForgeListener(EventPriority priority, Class<? super T> evtClass, Consumer<? super T> listener) {
        OneTimeEventReceiver.<T>addListener(MinecraftForge.EVENT_BUS, priority, evtClass, listener);
    }

    @Deprecated
    public static <T extends Event> void addListener(IEventBus bus, Class<? super T> evtClass, Consumer<? super T> listener) {
        OneTimeEventReceiver.<T>addListener(bus, EventPriority.NORMAL, evtClass, listener);
    }

    @SuppressWarnings("unchecked")
    @Deprecated
    public static <T extends Event> void addListener(IEventBus bus, EventPriority priority, Class<? super T> evtClass, Consumer<? super T> listener) {
        bus.addListener(priority, false, (Class<T>) evtClass, new OneTimeEventReceiver<>(bus, listener));
    }

    private static boolean seenModBus = false;
    private static final @Nullable MethodHandle getBusId;
    static {
        MethodHandle ret;
        try {
            ret = MethodHandles.lookup().unreflectGetter(ObfuscationReflectionHelper.findField(EventBus.class, "busID"));
        } catch (IllegalAccessException e) {
            log.warn("Failed to set up EventBus reflection to release one-time event listeners, leaks will occur. This is not a big deal.");
            ret = null;
        }
        getBusId = ret;
    }

    private final IEventBus bus;
    private final Consumer<? super T> listener;
    private final AtomicBoolean consumed = new AtomicBoolean();

    public OneTimeEventReceiver(IEventBus bus, Consumer<? super T> listener) {
        this.bus = bus;
        this.listener = listener;
    }

    @Override
    public void accept(T event) {
        if (consumed.compareAndSet(false, true)) {
            listener.accept(event);
            unregister(bus, this, event);
        }
    }

    private static final List<Triple<IEventBus, Object, Class<? extends Event>>> toUnregister = new ArrayList<>();

    private static synchronized void unregister(IEventBus bus, Object listener, Event event) {
        unregister(bus, listener, event.getClass());
    }

    public static synchronized void unregister(AbstractRegistrate<?> owner, Object listener, Class<? extends Event> event) {
        unregister(((AbstractRegistrateForgeExtension<?>) owner).getModEventBus(), listener, event);
    }

    private static synchronized void unregister(IEventBus bus, Object listener, Class<? extends Event> event) {
        toUnregister.add(Triple.of(bus, listener, event));
    }

    private static void onLoadComplete(FMLLoadCompleteEvent event) {
        event.enqueueWork(() -> {
            toUnregister.forEach(t -> {
                t.getLeft().unregister(t.getMiddle());
                try {
                    final MethodHandle mh = getBusId;
                    if (mh != null) {
                        EventListenerHelper.getListenerList(t.getRight()).getListeners((int) mh.invokeExact((EventBus) t.getLeft()));
                    }
                } catch (Throwable ex) {
                    log.warn("Failed to clear listener list of one-time event receiver, so the receiver has leaked. This is not a big deal.", ex);
                }
            });
            toUnregister.clear();
        });
    }
}