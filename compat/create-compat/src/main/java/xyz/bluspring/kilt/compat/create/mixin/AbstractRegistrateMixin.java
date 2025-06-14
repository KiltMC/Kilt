package xyz.bluspring.kilt.compat.create.mixin;

import com.google.common.collect.Multimap;
import com.google.common.collect.Table;
import com.moulberry.mixinconstraints.annotations.IfModLoaded;
import com.tterag.registrate.util.OneTimeEventReceiver;
import com.tterrag.registrate.AbstractRegistrate;
import com.tterrag.registrate.util.CreativeModeTabModifier;
import com.tterrag.registrate.util.DebugMarkers;
import com.tterrag.registrate.util.entry.RegistryEntry;
import com.tterrag.registrate.util.nullness.NonNullConsumer;
import com.tterrag.registrate.util.nullness.NonNullSupplier;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.RegisterEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import xyz.bluspring.kilt.compat.create.extensions.AbstractRegistrateForgeExtension;
import xyz.bluspring.kilt.compat.create.extensions.AbstractRegistrateRegistrationForgeExtension;
import xyz.bluspring.kilt.compat.create.extensions.RegistryEntryForgeExtension;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

@IfModLoaded("registrate-fabric")
@Mixin(AbstractRegistrate.class)
public abstract class AbstractRegistrateMixin<S extends AbstractRegistrate<S>> implements AbstractRegistrateForgeExtension<S> {
    @Shadow @Final private NonNullSupplier<Boolean> doDatagen;
    @Shadow @Final private Multimap<ResourceKey<CreativeModeTab>, Consumer<CreativeModeTabModifier>> creativeModeTabModifiers;
    @Shadow @Final private Multimap<Pair<String, ResourceKey<? extends Registry<?>>>, NonNullConsumer<?>> registerCallbacks;

    @Shadow public static boolean isDevEnvironment() {
        throw new IllegalStateException();
    }

    @Shadow @Final private Table registrations;
    @Shadow @Final private static Logger log;
    @Shadow private boolean skipErrors;

    @Shadow @Final private Multimap<ResourceKey<? extends Registry<?>>, Runnable> afterRegisterCallbacks;

    @Shadow @Final private Set<ResourceKey<? extends Registry<?>>> completedRegistrations;

    public IEventBus getModEventBus() {
        return FMLJavaModLoadingContext.get().getModEventBus();
    }

    protected void onData(GatherDataEvent event) {
        // TODO: properly implement this
        //event.getGenerator().addProvider(true, provider = new RegistrateDataProvider((AbstractRegistrate<?>) (Object) this, modid, event));
    }

    protected void onRegister(RegisterEvent event) {
        ResourceKey<? extends Registry<?>> type = event.getRegistryKey();
        if (!registerCallbacks.isEmpty()) {
            registerCallbacks.asMap().forEach((k, v) -> log.warn("Found {} unused register callback(s) for entry {} [{}]. Was the entry ever registered?", v.size(), k.getLeft(), k.getRight().location()));
            registerCallbacks.clear();
            if (isDevEnvironment()) {
                throw new IllegalStateException("Found unused register callbacks, see logs");
            }
        }
        Map registrationsForType = registrations.row(type);
        if (registrationsForType.size() > 0) {
            log.debug(DebugMarkers.REGISTER, "Registering {} known objects of type {}", registrationsForType.size(), type.location());
            for (Object entry : registrationsForType.entrySet()) {
                var e = (Map.Entry<?, ?>) entry;
                var value = ((AbstractRegistrateRegistrationForgeExtension<?, ?>) e.getValue());

                try {
                    value.register(event);
                    log.debug(DebugMarkers.REGISTER, "Registered {} to registry {}", value.getName(), type);
                } catch (Exception ex) {
                    String err = "Unexpected error while registering entry " + value.getName() + " to registry " + type;
                    if (skipErrors) {
                        log.error(DebugMarkers.REGISTER, err);
                    } else {
                        throw new RuntimeException(err, ex);
                    }
                }
            }
        }
    }

    protected void onRegisterLate(RegisterEvent event) {
        ResourceKey<? extends Registry<?>> type = event.getRegistryKey();
        Collection<Runnable> callbacks = afterRegisterCallbacks.get(type);
        callbacks.forEach(Runnable::run);
        callbacks.clear();
        completedRegistrations.add(type);
    }

    protected void onBuildCreativeModeTabContents(BuildCreativeModeTabContentsEvent event) {
        var modifier = new CreativeModeTabModifier(event::getFlags, event::hasPermissions, event::accept);

        creativeModeTabModifiers.forEach((key, value) -> {
            if (event.getTabKey().equals(key))
                value.accept(modifier);
        });
    }

    @Override
    public @NotNull S registerEventListeners(@NotNull IEventBus bus) {
        Consumer<RegisterEvent> onRegister = this::onRegister;
        Consumer<RegisterEvent> onRegisterLate = this::onRegisterLate;
        bus.addListener(onRegister);
        bus.addListener(EventPriority.LOWEST, onRegisterLate);
        bus.addListener(this::onBuildCreativeModeTabContents); // Fired multiple times when ever tabs need contents rebuilt (changing op tab perms for example)

        // Register events fire multiple times, so clean them up on common setup
        var self = (AbstractRegistrate<?>) (Object) this;
        OneTimeEventReceiver.addModListener(self, FMLCommonSetupEvent.class, $ -> {
            OneTimeEventReceiver.unregister(self, onRegister, RegisterEvent.class);
            OneTimeEventReceiver.unregister(self, onRegisterLate, RegisterEvent.class);
        });

        if (doDatagen.get()) {
            OneTimeEventReceiver.addModListener(self, GatherDataEvent.class, this::onData);
        }

        return (S) (Object) this;
    }

    @Mixin(targets = "com.tterrag.registrate.AbstractRegistrate$Registration")
    public abstract static class RegistrationMixin<R, T extends R> implements AbstractRegistrateRegistrationForgeExtension<R, T> {
        @Final @Shadow private NonNullSupplier<? extends T> creator;
        @Final @Shadow private ResourceKey<? extends Registry<R>> type;
        @Final @Shadow private ResourceLocation name;
        @Shadow private RegistryEntry<T> delegate;

        @Override
        public void register(@NotNull RegisterEvent event) {
            T entry = creator.get();
            var name = this.name;
            event.register(type, rh -> rh.register(name, entry));
            ((RegistryEntryForgeExtension) delegate).updateReference(event);
        }

        @Override
        public @NotNull ResourceLocation getName() {
            return name;
        }
    }
}
