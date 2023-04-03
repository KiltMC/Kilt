package xyz.bluspring.kilt.forgeinjects.client.multiplayer;

import com.llamalad7.mixinextras.injector.WrapWithCondition;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import net.minecraft.client.color.block.BlockTintCache;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ColorResolver;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.storage.WritableLevelData;
import net.minecraftforge.client.ColorResolverManager;
import net.minecraftforge.client.model.lighting.QuadLighter;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.level.LevelEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.Supplier;

@Mixin(ClientLevel.class)
public abstract class ClientLevelInject extends Level {
    @Shadow public abstract DimensionSpecialEffects effects();

    protected ClientLevelInject(WritableLevelData writableLevelData, ResourceKey<Level> resourceKey, Holder<DimensionType> holder, Supplier<ProfilerFiller> supplier, boolean bl, boolean bl2, long l, int i) {
        super(writableLevelData, resourceKey, holder, supplier, bl, bl2, l, i);
    }

    @Inject(at = @At("TAIL"), method = "method_23778")
    public void kilt$registerForgeBlockTintCaches(Object2ObjectArrayMap<ColorResolver, BlockTintCache> object2ObjectArrayMap, CallbackInfo ci) {
        ColorResolverManager.registerBlockTintCaches((ClientLevel) (Object) this, object2ObjectArrayMap);
    }
    @Inject(at = @At("TAIL"), method = "<init>")
    public void kilt$initLevel(ClientPacketListener clientPacketListener, ClientLevel.ClientLevelData clientLevelData, ResourceKey resourceKey, Holder holder, int i, int j, Supplier supplier, LevelRenderer levelRenderer, boolean bl, long l, CallbackInfo ci) {
        this.gatherCapabilities();
        MinecraftForge.EVENT_BUS.post(new LevelEvent.Load((ClientLevel) (Object) this));
    }

    @WrapWithCondition(method = "tickNonPassenger", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;tick()V"))
    public boolean kilt$onlyTickIfCanUpdate(Entity entity) {
        return entity.canUpdate();
    }

    @Inject(at = @At("HEAD"), method = "addEntity", cancellable = true)
    public void kilt$runJoinLevelEvent(int i, Entity entity, CallbackInfo ci) {
        if (MinecraftForge.EVENT_BUS.post(new EntityJoinLevelEvent(entity, (ClientLevel) (Object) this)))
            ci.cancel();
    }

    @Inject(at = @At("TAIL"), method = "addEntity")
    public void kilt$addEntityToWorld(int i, Entity entity, CallbackInfo ci) {
        entity.onAddedToWorld();
    }

    public float getShade(float normalX, float normalY, float normalZ, boolean shade) {
        var constantAmbientLight = this.effects().constantAmbientLight();

        if (!shade)
            return constantAmbientLight ? 0.9F : 1F;

        return QuadLighter.calculateShade(normalX, normalY, normalZ, constantAmbientLight);
    }
}
