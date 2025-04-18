package xyz.bluspring.kilt.compat.create.mixin;

import com.google.common.collect.Multimap;
import com.moulberry.mixinconstraints.annotations.IfModLoaded;
import com.tterag.registrate.util.OneTimeEventReceiver;
import com.tterrag.registrate.AbstractRegistrate;
import com.tterrag.registrate.providers.RegistrateDataProvider;
import com.tterrag.registrate.util.CreativeModeTabModifier;
import com.tterrag.registrate.util.nullness.NonNullSupplier;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.RegisterEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import xyz.bluspring.kilt.compat.create.AbstractRegistrateForgeExtension;

import java.util.function.Consumer;

@IfModLoaded("registrate")
@Mixin(AbstractRegistrate.class)
public abstract class AbstractRegistrateMixin<S extends AbstractRegistrate<S>> implements AbstractRegistrateForgeExtension<S> {
    @Shadow @Final private NonNullSupplier<Boolean> doDatagen;
    @Shadow protected abstract void onRegister(Registry<?> registry);
    @Shadow protected abstract void onRegisterLate(Registry<?> registry);
    @Shadow @Final private Multimap<ResourceKey<CreativeModeTab>, Consumer<CreativeModeTabModifier>> creativeModeTabModifiers;
    @Shadow private @Nullable RegistrateDataProvider provider;

    @Shadow @Final private String modid;

    public IEventBus getModEventBus() {
        return FMLJavaModLoadingContext.get().getModEventBus();
    }

    protected void onData(GatherDataEvent event) {
        // TODO: properly implement this
        //event.getGenerator().addProvider(true, provider = new RegistrateDataProvider((AbstractRegistrate<?>) (Object) this, modid, event));
    }

    protected void onRegister(RegisterEvent event) {
        this.onRegister(event.getVanillaRegistry());
    }

    protected void onRegisterLate(RegisterEvent event) {
        this.onRegisterLate(event.getVanillaRegistry());
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
}
