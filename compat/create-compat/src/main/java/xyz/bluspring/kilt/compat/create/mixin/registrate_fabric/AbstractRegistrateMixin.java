package xyz.bluspring.kilt.compat.create.mixin.registrate_fabric;

import com.google.common.collect.Multimap;
import com.google.common.collect.Table;
import com.moulberry.mixinconstraints.annotations.IfModLoaded;
import com.tterag.registrate.util.OneTimeEventReceiver;
import com.tterrag.registrate.AbstractRegistrate;
import com.tterrag.registrate.builders.Builder;
import com.tterrag.registrate.builders.BuilderCallback;
import com.tterrag.registrate.builders.FluidBuilder;
import com.tterrag.registrate.fabric.SimpleFlowableFluid;
import com.tterrag.registrate.util.CreativeModeTabModifier;
import com.tterrag.registrate.util.DebugMarkers;
import com.tterrag.registrate.util.entry.RegistryEntry;
import com.tterrag.registrate.util.nullness.NonNullConsumer;
import com.tterrag.registrate.util.nullness.NonNullFunction;
import com.tterrag.registrate.util.nullness.NonNullSupplier;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.ForgeFlowingFluid;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.RegisterEvent;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.Inject;
import xyz.bluspring.kilt.Kilt;
import xyz.bluspring.kilt.compat.create.extensions.AbstractRegistrateForgeExtension;
import xyz.bluspring.kilt.compat.create.extensions.AbstractRegistrateRegistrationForgeExtension;
import xyz.bluspring.kilt.compat.create.extensions.RegistryEntryForgeExtension;
import xyz.bluspring.kilt.compat.create.registrate.FluidBuilderHelper;
import xyz.bluspring.kilt.compat.create.registrate.FluidTypeFactoryToken;
import xyz.bluspring.kilt.compat.create.registrate.injects.AbstractRegistrateInjection;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

@IfModLoaded("registrate-fabric")
@Implements(@Interface(iface = AbstractRegistrateInjection.class, prefix = "kilt$i$"))
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

    @Shadow protected abstract S self();

    @Shadow public abstract <R, T extends R, P, S2 extends Builder<R, T, P, S2>> S2 entry(String name, NonNullFunction<BuilderCallback, S2> factory);

    @Shadow protected abstract String currentName();

    @Shadow public abstract String getModid();

    @Shadow
    public abstract FluidBuilder<SimpleFlowableFluid.Flowing, S> fluid();

    @Shadow
    public abstract FluidBuilder<SimpleFlowableFluid.Flowing, S> fluid(ResourceLocation stillTexture, ResourceLocation flowingTexture);

    @Shadow
    public abstract <T extends SimpleFlowableFluid> FluidBuilder<T, S> fluid(ResourceLocation stillTexture, ResourceLocation flowingTexture, NonNullFunction<SimpleFlowableFluid.Properties, T> fluidFactory);

    @Shadow
    public abstract FluidBuilder<SimpleFlowableFluid.Flowing, S> fluid(String name);

    @Shadow
    public abstract FluidBuilder<SimpleFlowableFluid.Flowing, S> fluid(String name, ResourceLocation stillTexture, ResourceLocation flowingTexture);

    @Shadow
    public abstract <T extends SimpleFlowableFluid> FluidBuilder<T, S> fluid(String name, ResourceLocation stillTexture, ResourceLocation flowingTexture, NonNullFunction<SimpleFlowableFluid.Properties, T> fluidFactory);

    @Shadow
    public abstract <P> FluidBuilder<SimpleFlowableFluid.Flowing, P> fluid(P parent);

    @Shadow
    public abstract <P> FluidBuilder<SimpleFlowableFluid.Flowing, P> fluid(P parent, ResourceLocation stillTexture, ResourceLocation flowingTexture);

    @Shadow
    public abstract <T extends SimpleFlowableFluid, P> FluidBuilder<T, P> fluid(P parent, ResourceLocation stillTexture, ResourceLocation flowingTexture, NonNullFunction<SimpleFlowableFluid.Properties, T> fluidFactory);

    @Shadow
    public abstract <T extends SimpleFlowableFluid, P> FluidBuilder<T, P> fluid(P parent, String name, ResourceLocation stillTexture, ResourceLocation flowingTexture, NonNullFunction<SimpleFlowableFluid.Properties, T> fluidFactory);

    @Shadow
    public abstract <P> FluidBuilder<SimpleFlowableFluid.Flowing, P> fluid(P parent, String name);

    @Shadow
    public abstract <P> FluidBuilder<SimpleFlowableFluid.Flowing, P> fluid(P parent, String name, ResourceLocation stillTexture, ResourceLocation flowingTexture);

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

    @IfModLoaded("registrate-fabric")
    @Mixin(targets = "com.tterrag.registrate.AbstractRegistrate$Registration")
    public abstract static class RegistrationMixin<R, T extends R> implements AbstractRegistrateRegistrationForgeExtension<R, T> {
        @Final @Shadow private NonNullSupplier<? extends T> creator;
        @Final @Shadow private ResourceKey<? extends Registry<R>> type;
        @Final @Shadow private ResourceLocation name;
        @Shadow private RegistryEntry<T> delegate;

        @Shadow
        private List<NonNullConsumer<? super T>> callbacks;

        @Override
        public void register(@NotNull RegisterEvent event) {
            T entry = creator.get();
            var name = this.name;
            event.register(type, rh -> rh.register(name, entry));
            ((RegistryEntryForgeExtension) delegate).updateReference(event);
            callbacks.forEach(c -> c.accept(entry));
            callbacks.clear();
        }

        @Override
        public @NotNull ResourceLocation getName() {
            return name;
        }
    }

    @Unique
    private boolean kilt$isForge() {
        return Kilt.Companion.getLoader().hasMod(this.getModid());
    }

    @Intrinsic(displace = true)
    public FluidBuilder/*<ForgeFlowingFluid.Flowing, S>*/ kilt$i$fluid() {
        if (!this.kilt$isForge()) return this.fluid();
        return this.kilt$i$fluid((Object)this.self());
    }

    public FluidBuilder/*<ForgeFlowingFluid.Flowing, S>*/ fluid(FluidTypeFactoryToken typeFactory) {
        return this.fluid((Object)this.self(), typeFactory);
    }

    @Intrinsic(displace = true)
    public FluidBuilder/*<ForgeFlowingFluid.Flowing, S>*/ kilt$i$fluid(NonNullSupplier<FluidType> fluidType) {
        return this.kilt$i$fluid((Object)this.self(), fluidType);
    }

    @Intrinsic(displace = true)
    public FluidBuilder/*<ForgeFlowingFluid.Flowing, S>*/ kilt$i$fluid(ResourceLocation stillTexture, ResourceLocation flowingTexture) {
        if (!this.kilt$isForge()) return this.fluid(stillTexture, flowingTexture);
        return this.kilt$i$fluid((Object)this.self(), stillTexture, flowingTexture);
    }

    public FluidBuilder/*<ForgeFlowingFluid.Flowing, S>*/ fluid(ResourceLocation stillTexture, ResourceLocation flowingTexture, FluidTypeFactoryToken typeFactory) {
        return this.fluid((Object)this.self(), stillTexture, flowingTexture, typeFactory);
    }

    @Intrinsic(displace = true)
    public FluidBuilder/*<ForgeFlowingFluid.Flowing, S>*/ kilt$i$fluid(ResourceLocation stillTexture, ResourceLocation flowingTexture, NonNullSupplier<FluidType> fluidType) {
        return this.kilt$i$fluid((Object)this.self(), stillTexture, flowingTexture, fluidType);
    }

    @Intrinsic(displace = true)
    public <T extends ForgeFlowingFluid> FluidBuilder/*<T, S>*/ kilt$i$fluid(ResourceLocation stillTexture, ResourceLocation flowingTexture, NonNullFunction<ForgeFlowingFluid.Properties, T> fluidFactory) {
        if (!this.kilt$isForge()) return this.fluid(stillTexture, flowingTexture, (NonNullFunction<SimpleFlowableFluid.Properties, ? extends SimpleFlowableFluid>) (Object) fluidFactory);
        return this.kilt$i$fluid((Object)this.self(), stillTexture, flowingTexture, fluidFactory);
    }

    public <T extends ForgeFlowingFluid> FluidBuilder/*<T, S>*/ fluid(ResourceLocation stillTexture, ResourceLocation flowingTexture, FluidTypeFactoryToken typeFactory, NonNullFunction<ForgeFlowingFluid.Properties, T> fluidFactory) {
        return this.fluid((Object)this.self(), stillTexture, flowingTexture, typeFactory, fluidFactory);
    }

    @Intrinsic(displace = true)
    public <T extends ForgeFlowingFluid> FluidBuilder/*<T, S>*/ kilt$i$fluid(ResourceLocation stillTexture, ResourceLocation flowingTexture, NonNullSupplier<FluidType> fluidType, NonNullFunction<ForgeFlowingFluid.Properties, T> fluidFactory) {
        return this.kilt$i$fluid((Object)this.self(), stillTexture, flowingTexture, fluidType, fluidFactory);
    }

    @Intrinsic(displace = true)
    public FluidBuilder/*<ForgeFlowingFluid.Flowing, S>*/ kilt$i$fluid(String name) {
        if (!this.kilt$isForge()) return this.fluid(name);
        return this.kilt$i$fluid((Object)this.self(), name);
    }

    public FluidBuilder/*<ForgeFlowingFluid.Flowing, S>*/ fluid(String name, FluidTypeFactoryToken typeFactory) {
        return this.fluid((Object)this.self(), name, typeFactory);
    }

    @Intrinsic(displace = true)
    public FluidBuilder/*<ForgeFlowingFluid.Flowing, S>*/ kilt$i$fluid(String name, NonNullSupplier<FluidType> fluidType) {
        return this.kilt$i$fluid((Object)this.self(), name, fluidType);
    }

    @Intrinsic(displace = true)
    public FluidBuilder/*<ForgeFlowingFluid.Flowing, S>*/ kilt$i$fluid(String name, ResourceLocation stillTexture, ResourceLocation flowingTexture) {
        if (!this.kilt$isForge()) return this.fluid(name, stillTexture, flowingTexture);
        return this.kilt$i$fluid((Object)this.self(), name, stillTexture, flowingTexture);
    }

    public FluidBuilder/*<ForgeFlowingFluid.Flowing, S>*/ fluid(String name, ResourceLocation stillTexture, ResourceLocation flowingTexture, FluidTypeFactoryToken typeFactory) {
        return this.fluid((Object)this.self(), name, stillTexture, flowingTexture, typeFactory);
    }

    @Intrinsic(displace = true)
    public FluidBuilder/*<ForgeFlowingFluid.Flowing, S>*/ kilt$i$fluid(String name, ResourceLocation stillTexture, ResourceLocation flowingTexture, NonNullSupplier<FluidType> fluidType) {
        return this.kilt$i$fluid((Object)this.self(), name, stillTexture, flowingTexture, fluidType);
    }

    @Intrinsic(displace = true)
    public <T extends ForgeFlowingFluid> FluidBuilder/*<T, S>*/ kilt$i$fluid(String name, ResourceLocation stillTexture, ResourceLocation flowingTexture, NonNullFunction<ForgeFlowingFluid.Properties, T> fluidFactory) {
        if (!this.kilt$isForge()) return this.fluid(name, stillTexture, flowingTexture, (NonNullFunction<SimpleFlowableFluid.Properties, ? extends SimpleFlowableFluid>) (Object) fluidFactory);
        return this.kilt$i$fluid((Object)this.self(), name, stillTexture, flowingTexture, fluidFactory);
    }

    public <T extends ForgeFlowingFluid> FluidBuilder/*<T, S>*/ fluid(String name, ResourceLocation stillTexture, ResourceLocation flowingTexture, FluidTypeFactoryToken typeFactory, NonNullFunction<ForgeFlowingFluid.Properties, T> fluidFactory) {
        return this.fluid(this.self(), name, stillTexture, flowingTexture, typeFactory, fluidFactory);
    }

    @Intrinsic(displace = true)
    public <T extends ForgeFlowingFluid> FluidBuilder/*<T, S>*/ kilt$i$fluid(String name, ResourceLocation stillTexture, ResourceLocation flowingTexture, NonNullSupplier<FluidType> fluidType, NonNullFunction<ForgeFlowingFluid.Properties, T> fluidFactory) {
        return this.kilt$i$fluid(this.self(), name, stillTexture, flowingTexture, fluidType, fluidFactory);
    }

    @Intrinsic(displace = true)
    public <P> FluidBuilder/*<ForgeFlowingFluid.Flowing, P>*/ kilt$i$fluid(P parent) {
        if (!this.kilt$isForge()) return this.fluid(parent);
        return this.kilt$i$fluid(parent, this.currentName());
    }

    public <P> FluidBuilder/*<ForgeFlowingFluid.Flowing, P>*/ fluid(P parent, FluidTypeFactoryToken typeFactory) {
        return this.fluid(parent, this.currentName(), typeFactory);
    }

    @Intrinsic(displace = true)
    public <P> FluidBuilder/*<ForgeFlowingFluid.Flowing, P>*/ kilt$i$fluid(P parent, NonNullSupplier<FluidType> fluidType) {
        return this.kilt$i$fluid(parent, this.currentName(), fluidType);
    }

    @Intrinsic(displace = true)
    public <P> FluidBuilder/*<ForgeFlowingFluid.Flowing, P>*/ kilt$i$fluid(P parent, ResourceLocation stillTexture, ResourceLocation flowingTexture) {
        if (!this.kilt$isForge()) return this.fluid(parent, stillTexture, flowingTexture);
        return this.kilt$i$fluid(parent, this.currentName(), stillTexture, flowingTexture);
    }

    public <P> FluidBuilder/*<ForgeFlowingFluid.Flowing, P>*/ fluid(P parent, ResourceLocation stillTexture, ResourceLocation flowingTexture, FluidTypeFactoryToken typeFactory) {
        return this.fluid(parent, this.currentName(), stillTexture, flowingTexture, typeFactory);
    }

    @Intrinsic(displace = true)
    public <P> FluidBuilder/*<ForgeFlowingFluid.Flowing, P>*/ kilt$i$fluid(P parent, ResourceLocation stillTexture, ResourceLocation flowingTexture, NonNullSupplier<FluidType> fluidType) {
        return this.kilt$i$fluid(parent, this.currentName(), stillTexture, flowingTexture, fluidType);
    }

    @Intrinsic(displace = true)
    public <T extends ForgeFlowingFluid, P> FluidBuilder/*<T, P>*/ kilt$i$fluid(P parent, ResourceLocation stillTexture, ResourceLocation flowingTexture, NonNullFunction<ForgeFlowingFluid.Properties, T> fluidFactory) {
        if (!this.kilt$isForge()) return this.fluid(parent, stillTexture, flowingTexture, (NonNullFunction<SimpleFlowableFluid.Properties, ? extends SimpleFlowableFluid>) (Object) fluidFactory);
        return this.kilt$i$fluid(parent, this.currentName(), stillTexture, flowingTexture, fluidFactory);
    }

    public <T extends ForgeFlowingFluid, P> FluidBuilder/*<T, P>*/ fluid(P parent, ResourceLocation stillTexture, ResourceLocation flowingTexture, FluidTypeFactoryToken typeFactory, NonNullFunction<ForgeFlowingFluid.Properties, T> fluidFactory) {
        return this.fluid(parent, this.currentName(), stillTexture, flowingTexture, typeFactory, fluidFactory);
    }

    @Intrinsic(displace = true)
    public <T extends ForgeFlowingFluid, P> FluidBuilder/*<T, P>*/ kilt$i$fluid(P parent, ResourceLocation stillTexture, ResourceLocation flowingTexture, NonNullSupplier<FluidType> fluidType, NonNullFunction<ForgeFlowingFluid.Properties, T> fluidFactory) {
        return this.kilt$i$fluid(parent, this.currentName(), stillTexture, flowingTexture, fluidType, fluidFactory);
    }

    @Intrinsic(displace = true)
    public <P> FluidBuilder/*<ForgeFlowingFluid.Flowing, P>*/ kilt$i$fluid(P parent, String name) {
        if (!this.kilt$isForge()) return this.fluid(parent, name);
        return this.kilt$i$fluid(parent, name, new ResourceLocation(this.getModid(), "block/" + this.currentName() + "_still"), new ResourceLocation(this.getModid(), "block/" + this.currentName() + "_flow"));
    }

    public <P> FluidBuilder/*<ForgeFlowingFluid.Flowing, P>*/ fluid(P parent, String name, FluidTypeFactoryToken typeFactory) {
        return this.fluid(parent, name, new ResourceLocation(this.getModid(), "block/" + this.currentName() + "_still"), new ResourceLocation(this.getModid(), "block/" + this.currentName() + "_flow"), typeFactory);
    }

    @Intrinsic(displace = true)
    public <P> FluidBuilder/*<ForgeFlowingFluid.Flowing, P>*/ kilt$i$fluid(P parent, String name, NonNullSupplier<FluidType> fluidType) {
        return this.kilt$i$fluid(parent, name, new ResourceLocation(this.getModid(), "block/" + this.currentName() + "_still"), new ResourceLocation(this.getModid(), "block/" + this.currentName() + "_flow"), fluidType);
    }

    @Intrinsic(displace = true)
    public <P> FluidBuilder/*<ForgeFlowingFluid.Flowing, P>*/ kilt$i$fluid(P parent, String name, ResourceLocation stillTexture, ResourceLocation flowingTexture) {
        if (!this.kilt$isForge()) return this.fluid(parent, name, stillTexture, flowingTexture);
        return this.entry(name, (callback) -> FluidBuilderHelper.createFluidBuilder((AbstractRegistrate<S>) (Object) this, parent, name, callback, stillTexture, flowingTexture));
    }

    public <P> FluidBuilder/*<ForgeFlowingFluid.Flowing, P>*/ fluid(P parent, String name, ResourceLocation stillTexture, ResourceLocation flowingTexture,FluidTypeFactoryToken typeFactory) {
        return this.entry(name, (callback) -> FluidBuilderHelper.createFluidBuilder((AbstractRegistrate<S>) (Object) this, parent, name, callback, stillTexture, flowingTexture, typeFactory));
    }

    @Intrinsic(displace = true)
    public <P> FluidBuilder/*<ForgeFlowingFluid.Flowing, P>*/ kilt$i$fluid(P parent, String name, ResourceLocation stillTexture, ResourceLocation flowingTexture, NonNullSupplier<FluidType> fluidType) {
        return this.entry(name, (callback) -> FluidBuilderHelper.createFluidBuilder((AbstractRegistrate<S>) (Object) this, parent, name, callback, stillTexture, flowingTexture, fluidType));
    }

    @Intrinsic(displace = true)
    public <T extends ForgeFlowingFluid, P> FluidBuilder/*<T, P>*/ kilt$i$fluid(P parent, String name, ResourceLocation stillTexture, ResourceLocation flowingTexture, NonNullFunction<ForgeFlowingFluid.Properties, T> fluidFactory) {
        if (!this.kilt$isForge()) return this.fluid(parent, name, stillTexture, flowingTexture, (NonNullFunction<SimpleFlowableFluid.Properties, ? extends SimpleFlowableFluid>) (Object) fluidFactory);
        return this.entry(name, (callback) -> FluidBuilderHelper.createFluidBuilder((AbstractRegistrate<S>) (Object) this, parent, name, callback, stillTexture, flowingTexture, fluidFactory));
    }

    public <T extends ForgeFlowingFluid, P> FluidBuilder/*<T, P>*/ fluid(P parent, String name, ResourceLocation stillTexture, ResourceLocation flowingTexture, FluidTypeFactoryToken typeFactory, NonNullFunction<ForgeFlowingFluid.Properties, T> fluidFactory) {
        return this.entry(name, (callback) -> FluidBuilderHelper.createFluidBuilder((AbstractRegistrate<S>) (Object) this, parent, name, callback, stillTexture, flowingTexture, typeFactory, fluidFactory));
    }

    @Intrinsic(displace = true)
    public <T extends ForgeFlowingFluid, P> FluidBuilder/*<T, P>*/ kilt$i$fluid(P parent, String name, ResourceLocation stillTexture, ResourceLocation flowingTexture, NonNullSupplier<FluidType> fluidType, NonNullFunction<ForgeFlowingFluid.Properties, T> fluidFactory) {
        return this.entry(name, (callback) -> FluidBuilderHelper.createFluidBuilder((AbstractRegistrate<S>) (Object) this, parent, name, callback, stillTexture, flowingTexture, fluidType, fluidFactory));
    }
}
