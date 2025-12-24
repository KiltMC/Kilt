package xyz.bluspring.kilt.compat.create.mixin.registrate_fabric;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.moulberry.mixinconstraints.annotations.IfModLoaded;
import com.tterrag.registrate.AbstractRegistrate;
import com.tterrag.registrate.builders.AbstractBuilder;
import com.tterrag.registrate.builders.Builder;
import com.tterrag.registrate.builders.BuilderCallback;
import com.tterrag.registrate.builders.FluidBuilder;
import com.tterrag.registrate.fabric.SimpleFlowableFluid;
import com.tterrag.registrate.providers.ProviderType;
import com.tterrag.registrate.util.entry.RegistryEntry;
import com.tterrag.registrate.util.nullness.NonNullBiConsumer;
import com.tterrag.registrate.util.nullness.NonNullConsumer;
import com.tterrag.registrate.util.nullness.NonNullFunction;
import com.tterrag.registrate.util.nullness.NonNullSupplier;
import net.minecraft.Util;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.ForgeFlowingFluid;
import net.minecraftforge.registries.ForgeRegistries;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.bluspring.kilt.Kilt;
import xyz.bluspring.kilt.compat.create.registrate.FluidBuilderHelper;
import xyz.bluspring.kilt.compat.create.registrate.FluidTypeExtensionHelper;
import xyz.bluspring.kilt.compat.create.registrate.FluidTypeFactoryToken;
import xyz.bluspring.kilt.compat.create.registrate.SimpleWrappedForgeFlowingFluid;
import xyz.bluspring.kilt.compat.create.registrate.injects.FluidBuilderInjection;
import xyz.bluspring.kilt.helpers.mixin.CreateInitializer;
import xyz.bluspring.kilt.helpers.mixin.CreateStatic;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("rawtypes")
@IfModLoaded("registrate-fabric")
@Mixin(FluidBuilder.class)
public abstract class FluidBuilderMixin<T extends ForgeFlowingFluid, P> extends AbstractBuilder implements FluidBuilderInjection {
    @Shadow @Final @Mutable
    private NonNullFunction<ForgeFlowingFluid.Properties, T> fluidFactory;
    @Shadow @Final @Mutable
    private ResourceLocation flowingTexture;
    @Shadow @Final @Mutable
    private ResourceLocation stillTexture;
    @Shadow @Final @Mutable
    private String bucketName;
    @Shadow @Final @Mutable
    private String sourceName;

    @Shadow
    @Nullable
    private NonNullSupplier<? extends SimpleFlowableFluid> source;
    @Mutable @Shadow @Final
    private List<TagKey<Fluid>> tags;
    @Nullable
    private final NonNullSupplier<FluidType> fluidType;
    private NonNullConsumer<FluidType.Properties> typeProperties = NonNullConsumer.noop();
    private boolean registerType;
    private NonNullConsumer<ForgeFlowingFluid.Properties> kilt$fluidProperties;

    @Unique private boolean kilt$isForge = false;

    @Override
    public ResourceLocation kilt$getStillTexture() {
        return stillTexture;
    }

    @Override
    public ResourceLocation kilt$getFlowingTexture() {
        return flowingTexture;
    }

    @CreateInitializer
    public FluidBuilderMixin(AbstractRegistrate<?> owner, P parent, String name, BuilderCallback callback, ResourceLocation stillTexture, ResourceLocation flowingTexture, FluidTypeFactoryToken typeFactory, NonNullFunction<ForgeFlowingFluid.Properties, T> fluidFactory) {
        super(owner, parent, "flowing_" + name, callback, ForgeRegistries.Keys.FLUIDS);
        this.kilt$isForge = true;
        this.sourceName = name;
        this.bucketName = name + "_bucket";
        this.stillTexture = stillTexture;
        this.flowingTexture = flowingTexture;
        this.fluidFactory = fluidFactory;
        var self = (FluidBuilderInjection) (Object) this;
        this.fluidType = FluidBuilderHelper.createFluidTypeSupplier(typeFactory, self);
        this.registerType = true;
        this.tags = new ArrayList<>();

        String bucketName = this.bucketName;
        this.kilt$fluidProperties = FluidBuilderHelper.createPropertiesConsumer(owner, name, bucketName);
    }

    @CreateInitializer
    public FluidBuilderMixin(AbstractRegistrate<?> owner, P parent, String name, BuilderCallback callback, ResourceLocation stillTexture, ResourceLocation flowingTexture, NonNullSupplier<FluidType> fluidType, NonNullFunction<ForgeFlowingFluid.Properties, T> fluidFactory) {
        super(owner, parent, "flowing_" + name, callback, ForgeRegistries.Keys.FLUIDS);
        this.kilt$isForge = true;
        this.sourceName = name;
        this.bucketName = name + "_bucket";
        this.stillTexture = stillTexture;
        this.flowingTexture = flowingTexture;
        this.fluidFactory = fluidFactory;
        this.fluidType = fluidType;
        this.registerType = false; // Don't register if we have a fluid from outside.
        this.tags = new ArrayList<>();

        String bucketName = this.bucketName;
        this.kilt$fluidProperties = FluidBuilderHelper.createPropertiesConsumer(owner, name, bucketName);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void kilt$markForgeIfModIdIsForge(AbstractRegistrate owner, Object parent, String name, BuilderCallback callback, ResourceLocation stillTexture, ResourceLocation flowingTexture, NonNullFunction fluidFactory, CallbackInfo ci) {
        if (Kilt.Companion.getLoader().hasMod(owner.getModid())) {
            this.kilt$isForge = true;
        }
    }

    @CreateStatic
    private static <P> FluidBuilder kilt$create(AbstractRegistrate<?> owner, P parent, String name, BuilderCallback callback,
                                         ResourceLocation stillTexture, ResourceLocation flowingTexture)
    {
        return kilt$create(owner, parent, name, callback, stillTexture, flowingTexture, FluidTypeExtensionHelper::defaultFluidType, ForgeFlowingFluid.Flowing::new);
    }

    @CreateStatic
    private static <P> FluidBuilder kilt$create(AbstractRegistrate<?> owner, P parent, String name, BuilderCallback callback,
                                         ResourceLocation stillTexture, ResourceLocation flowingTexture,
                                         FluidTypeFactoryToken /* at runtime this will be turned into FluidBuilder$FluidTypeFactory */ typeFactory)
    {
        return kilt$create(owner, parent, name, callback, stillTexture, flowingTexture, typeFactory, ForgeFlowingFluid.Flowing::new);
    }

    @CreateStatic
    private static <P> FluidBuilder kilt$create(AbstractRegistrate<?> owner, P parent, String name, BuilderCallback callback, ResourceLocation stillTexture,
                                         ResourceLocation flowingTexture, NonNullSupplier<FluidType> fluidType)
    {
        return kilt$create(owner, parent, name, callback, stillTexture, flowingTexture, fluidType, ForgeFlowingFluid.Flowing::new);
    }

    @CreateStatic
    private static <T extends ForgeFlowingFluid, P> FluidBuilder kilt$create(AbstractRegistrate<?> owner, P parent, String name, BuilderCallback callback,
                                                                      ResourceLocation stillTexture, ResourceLocation flowingTexture, NonNullFunction<ForgeFlowingFluid.Properties, T> fluidFactory)
    {
        return kilt$create(owner, parent, name, callback, stillTexture, flowingTexture, FluidTypeExtensionHelper::defaultFluidType, fluidFactory);
    }

    @CreateStatic
    private static <T extends ForgeFlowingFluid, P> FluidBuilder kilt$create(AbstractRegistrate<?> owner, P parent, String name, BuilderCallback callback, ResourceLocation stillTexture,
                                                                      ResourceLocation flowingTexture, FluidTypeFactoryToken /* at runtime this will be turned into FluidBuilder$FluidTypeFactory */ typeFactory,
                                                                      NonNullFunction<ForgeFlowingFluid.Properties, T> fluidFactory)
    {
        FluidBuilder ret = FluidBuilderHelper.createFluidBuilder(owner, parent, name, callback, stillTexture, flowingTexture, typeFactory, fluidFactory)
                .defaultLang().defaultSource().defaultBlock().defaultBucket();
        return ret;
    }

    @CreateStatic
    private static <T extends ForgeFlowingFluid, P> FluidBuilder kilt$create(AbstractRegistrate<?> owner, P parent, String name, BuilderCallback callback, ResourceLocation stillTexture,
                                                                             ResourceLocation flowingTexture, NonNullSupplier<FluidType> fluidType, NonNullFunction<ForgeFlowingFluid.Properties, T> fluidFactory)
    {
        FluidBuilder ret = FluidBuilderHelper.createFluidBuilder(owner, parent, name, callback, stillTexture, flowingTexture, fluidType, fluidFactory)
                .defaultLang().defaultSource().defaultBlock().defaultBucket();
        return ret;
    }

    @Override
    public FluidType.Properties kilt$makeTypeProperties() {
        FluidType.Properties properties = FluidType.Properties.create();
        RegistryEntry<Block> block = getOwner().getOptional(sourceName, ForgeRegistries.Keys.BLOCKS);
        this.typeProperties.accept(properties);

        // Force the translation key after the user callback runs
        // This is done because we need to remove the lang data generator if using the block key,
        // and if it was possible to undo this change, it might result in the user translation getting
        // silently lost, as there's no good way to check whether the translation key was changed.
        // TODO improve this?
        if (block.isPresent()) {
            properties.descriptionId(block.get().getDescriptionId());
            setData(ProviderType.LANG, NonNullBiConsumer.noop());
        } else {
            properties.descriptionId(Util.makeDescriptionId("fluid", new ResourceLocation(getOwner().getModid(), sourceName)));
        }

        return properties;
    }

    @Unique
    private ForgeFlowingFluid.Properties kilt$makeForgeProperties() {
        NonNullSupplier<? extends SimpleFlowableFluid> source = this.source;
        ForgeFlowingFluid.Properties ret = new ForgeFlowingFluid.Properties(this.fluidType, source == null ? null : source::get, asSupplier());
        this.kilt$fluidProperties.accept(ret);
        return ret;
    }

    @Inject(method = "createEntry()Ljava/lang/Object;", at = @At("HEAD"), cancellable = true, remap = false)
    private void kilt$tryCreateForgeEntry(CallbackInfoReturnable<Object> cir) {
        if (this.kilt$isForge) {
            cir.setReturnValue(this.fluidFactory.apply(this.kilt$makeForgeProperties()));
        }
    }

    @Inject(method = "lambda$source$6", at = @At("HEAD"), remap = false, cancellable = true)
    private void kilt$tryCreateWrapped(NonNullFunction factory, CallbackInfoReturnable<SimpleFlowableFluid> cir) {
        if (this.kilt$isForge) {
            cir.setReturnValue(new SimpleWrappedForgeFlowingFluid((ForgeFlowingFluid) factory.apply(this.kilt$makeForgeProperties())));
        }
    }

    @WrapOperation(method = "register()Lcom/tterrag/registrate/util/entry/FluidEntry;", at = @At(value = "INVOKE", target = "Lcom/tterrag/registrate/builders/FluidBuilder;onRegister(Lcom/tterrag/registrate/util/nullness/NonNullConsumer;)Lcom/tterrag/registrate/builders/Builder;"), remap = false)
    private Builder kilt$preventRegisteringAttributesForForge(FluidBuilder instance, NonNullConsumer nonNullConsumer, Operation<Builder> original) {
        if (!this.kilt$isForge) {
            return original.call(instance, nonNullConsumer);
        }

        return null;
    }

    @WrapOperation(method = "register()Lcom/tterrag/registrate/util/entry/FluidEntry;", at = @At(value = "INVOKE", target = "Lcom/tterrag/registrate/builders/AbstractBuilder;register()Lcom/tterrag/registrate/util/entry/RegistryEntry;"), remap = false)
    private RegistryEntry<T> kilt$useAlternativeRegisterForForge(FluidBuilder instance, Operation<RegistryEntry<T>> original) {
        if (this.kilt$isForge) {
            this.getCallback().accept(this.getName(), this.getRegistryKey(), this, () -> this.fluidFactory.apply(this.kilt$makeForgeProperties()), this::createEntryWrapper);
        }

        return original.call(instance);
    }
}

