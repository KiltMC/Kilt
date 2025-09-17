// TRACKED HASH: 8ce7cfcc1608a79d687631411c28c60d1064aad3
package xyz.bluspring.kilt.forgeinjects.server.level;

import com.bawnorton.mixinsquared.TargetHandler;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Cancellable;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.mojang.authlib.GameProfile;
import io.github.fabricators_of_create.porting_lib.entity.ITeleporter;
import io.github.fabricators_of_create.porting_lib.entity.mixin.common.ServerPlayerMixin;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundTabListPacket;
import net.minecraft.network.protocol.game.ServerboundClientInformationPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.extensions.IForgeEntity;
import net.minecraftforge.entity.PartEntity;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.entity.player.PlayerContainerEvent;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.bluspring.kilt.injections.server.level.ServerPlayerInjection;
import xyz.bluspring.kilt.util.KiltHelper;

import java.util.Objects;
import java.util.OptionalInt;

@Mixin(value = ServerPlayer.class, priority = 1100)
public abstract class ServerPlayerInject extends Player implements ServerPlayerInjection {
    @Shadow public ServerGamePacketListenerImpl connection;
    @Shadow @Final public ServerPlayerGameMode gameMode;
    @Shadow private @Nullable Entity camera;
    @Shadow public abstract ServerLevel serverLevel();

    public ServerPlayerInject(Level level, BlockPos pos, float yRot, GameProfile gameProfile) {
        super(level, pos, yRot, gameProfile);
    }

    @Inject(method = "die", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;gameEvent(Lnet/minecraft/world/level/gameevent/GameEvent;)V", shift = At.Shift.AFTER), cancellable = true)
    private void kilt$callForgeLivingDeath(DamageSource damageSource, CallbackInfo ci) {
        if (ForgeHooks.onLivingDeath(this, damageSource))
            ci.cancel();
    }

    // This lacks the ITeleporter patches, because that patch handled by Porting Lib instead.
    @Inject(method = "changeDimension", at = @At("HEAD"), cancellable = true)
    private void kilt$callTravelToDimensionEventVanilla(ServerLevel destination, CallbackInfoReturnable<Entity> cir, @Share("entryLevel") LocalRef<ServerLevel> entryLevel) {
        entryLevel.set(this.serverLevel());

        if (!ForgeHooks.onTravelToDimension(this, destination.dimension()))
            cir.setReturnValue(null);
    }

    @WrapOperation(method = "changeDimension", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;unsetRemoved()V"))
    private void kilt$useReviveCallOnChangeDimensionVanilla(ServerPlayer instance, Operation<Void> original) {
        if (KiltHelper.INSTANCE.hasMethodOverride(instance.getClass(), IForgeEntity.class, "revive")) {
            this.revive();
        } else {
            original.call(instance);
            this.reviveCaps();
        }
    }

    @Inject(method = "changeDimension", at = @At(value = "FIELD", target = "Lnet/minecraft/server/level/ServerPlayer;lastSentFood:I", shift = At.Shift.AFTER))
    private void kilt$firePlayerChangedDimensionEventVanilla(ServerLevel destination, CallbackInfoReturnable<Entity> cir,  @Share("entryLevel") LocalRef<ServerLevel> entryLevel) {
        ForgeEventFactory.firePlayerChangedDimensionEvent((ServerPlayer) (Object) this, entryLevel.get().dimension(), destination.dimension());
    }

    // Porting Lib injects for the changeDimension patch
    @TargetHandler(mixin = "io.github.fabricators_of_create.porting_lib.entity.mixin.common.ServerPlayerMixin", name = "changeDimension(Lnet/minecraft/server/level/ServerLevel;Lio/github/fabricators_of_create/porting_lib/entity/ITeleporter;)Lnet/minecraft/world/entity/Entity;")
    @Inject(method = "@MixinSquared:Handler", at = @At("HEAD"), cancellable = true)
    private void kilt$onTravelToDimension(ServerLevel pDestination, ITeleporter teleporter, CallbackInfoReturnable<Entity> cir) {
        if (!ForgeHooks.onTravelToDimension(this, pDestination.dimension()))
            cir.setReturnValue(null);
    }

    @SuppressWarnings({"InvalidInjectorMethodSignature", "MixinAnnotationTarget"}) // We cannot target the "ServerPlayerMixin", so ServerPlayer is the closest we can get.
    @TargetHandler(mixin = "io.github.fabricators_of_create.porting_lib.entity.mixin.common.ServerPlayerMixin", name = "changeDimension(Lnet/minecraft/server/level/ServerLevel;Lio/github/fabricators_of_create/porting_lib/entity/ITeleporter;)Lnet/minecraft/world/entity/Entity;")
    @WrapOperation(method = "@MixinSquared:Handler", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;unsetRemoved()V"))
    private void kilt$handleRevive(ServerPlayer instance, Operation<Void> original) {
        if (KiltHelper.INSTANCE.hasMethodOverride(instance.getClass(), IForgeEntity.class, "revive")) {
            this.revive();
        } else {
            original.call(instance);
            this.reviveCaps();
        }
    }

    @SuppressWarnings("MixinAnnotationTarget") // same for over here.
    @TargetHandler(mixin = "io.github.fabricators_of_create.porting_lib.entity.mixin.common.ServerPlayerMixin", name = "changeDimension(Lnet/minecraft/server/level/ServerLevel;Lio/github/fabricators_of_create/porting_lib/entity/ITeleporter;)Lnet/minecraft/world/entity/Entity;")
    @Inject(method = "@MixinSquared:Handler", at = @At(value = "FIELD", target = "Lnet/minecraft/server/level/ServerPlayer;lastSentFood:I", shift = At.Shift.AFTER))
    private void kilt$firePlayerChangedDimension(ServerLevel pDestination, ITeleporter teleporter, CallbackInfoReturnable<Entity> cir, @Local ResourceKey<Level> resourcekey) {
        ForgeEventFactory.firePlayerChangedDimensionEvent(this, resourcekey, pDestination.dimension());
    }

    // Handled by Fabric API
    /*@Inject(at = @At("HEAD"), method = "startSleepInBed", cancellable = true)
    public void kilt$checkPlayerSleepEvent(BlockPos blockPos, CallbackInfoReturnable<Either<Player.BedSleepingProblem, Unit>> cir) {
        var ret = ForgeEventFactory.onPlayerSleepInBed((ServerPlayer) (Object) this, Optional.of(blockPos));
        if (ret != null)
            cir.setReturnValue(Either.left(ret));
    }*/

    @Inject(method = "openMenu", at = @At(value = "INVOKE", target = "Ljava/util/OptionalInt;of(I)Ljava/util/OptionalInt;"))
    private void kilt$callContainerOpenEvent(MenuProvider menu, CallbackInfoReturnable<OptionalInt> cir) {
        MinecraftForge.EVENT_BUS.post(new PlayerContainerEvent.Open(this, this.containerMenu));
    }

    @Inject(method = "openHorseInventory", at = @At("TAIL"))
    private void kilt$callContainerOpenEvent(AbstractHorse horse, Container inventory, CallbackInfo ci) {
        MinecraftForge.EVENT_BUS.post(new PlayerContainerEvent.Open(this, this.containerMenu));
    }

    @Inject(method = "doCloseContainer", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/inventory/InventoryMenu;transferState(Lnet/minecraft/world/inventory/AbstractContainerMenu;)V", shift = At.Shift.AFTER))
    private void kilt$callContainerCloseEvent(CallbackInfo ci) {
        MinecraftForge.EVENT_BUS.post(new PlayerContainerEvent.Close(this, this.containerMenu));
    }

    @Inject(method = "restoreFrom", at = @At("TAIL"))
    private void kilt$storeTabListData(ServerPlayer that, boolean keepEverything, CallbackInfo ci) {
        ForgeEventFactory.onPlayerClone(this, that, !keepEverything);
        this.tabListHeader = ((ServerPlayerInjection) that).getTabListHeader();
        this.tabListFooter = ((ServerPlayerInjection) that).getTabListFooter();
    }

    @ModifyVariable(method = "setGameMode", at = @At("HEAD"), argsOnly = true)
    private GameType kilt$callChangeGameType(GameType value, @Cancellable CallbackInfoReturnable<Boolean> cir) {
        value = ForgeHooks.onChangeGameType(this, this.gameMode.getGameModeForPlayer(), value);

        if (value == null) {
            cir.setReturnValue(false);
            return null;
        }

        return value;
    }

    @Inject(method = "updateOptions", at = @At("TAIL"))
    private void kilt$addLanguageData(ServerboundClientInformationPacket packet, CallbackInfo ci) {
        this.language = packet.language();
    }

    @Inject(method = "setCamera", at = @At(value = "FIELD", target = "Lnet/minecraft/server/level/ServerPlayer;camera:Lnet/minecraft/world/entity/Entity;", ordinal = 0))
    private void kilt$usePartEntityParentCamera(Entity entityToSpectate, CallbackInfo ci) {
        while (this.camera instanceof PartEntity<?> partEntity)
            this.camera = partEntity.getParent();
    }

    @ModifyReturnValue(method = "getTabListDisplayName", at = @At("RETURN"))
    private Component kilt$addTabListDisplayName(Component original) {
        if (!this.hasTabListName) {
            this.tabListDisplayName = ForgeEventFactory.getPlayerTabListDisplayName(this);
            this.hasTabListName = true;
        }

        if (this.tabListDisplayName == null)
            return original;

        return this.tabListDisplayName;
    }

    @Inject(method = "teleportTo(Lnet/minecraft/server/level/ServerLevel;DDDFF)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;serverLevel()Lnet/minecraft/server/level/ServerLevel;", ordinal = 0), cancellable = true)
    private void kilt$cancelIfDimensionEventCancelled(ServerLevel newLevel, double x, double y, double z, float yaw, float pitch, CallbackInfo ci) {
        if (!ForgeHooks.onTravelToDimension(this, newLevel.dimension()))
            ci.cancel();
    }

    @WrapOperation(method = "teleportTo(Lnet/minecraft/server/level/ServerLevel;DDDFF)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;unsetRemoved()V"))
    private void kilt$useReviveCall(ServerPlayer instance, Operation<Void> original) {
        if (KiltHelper.INSTANCE.hasMethodOverride(instance.getClass(), IForgeEntity.class, "revive")) {
            this.revive();
        } else {
            original.call(instance);
            this.reviveCaps();
        }
    }

    @Inject(method = "teleportTo(Lnet/minecraft/server/level/ServerLevel;DDDFF)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerGamePacketListenerImpl;teleport(DDDFF)V", shift = At.Shift.AFTER))
    private void kilt$setGameModeLevel(ServerLevel newLevel, double x, double y, double z, float yaw, float pitch, CallbackInfo ci) {
        this.gameMode.setLevel(newLevel);
    }

    @Inject(method = "teleportTo(Lnet/minecraft/server/level/ServerLevel;DDDFF)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/players/PlayerList;sendAllPlayerInfo(Lnet/minecraft/server/level/ServerPlayer;)V"))
    private void kilt$fireChangeDimensionEvent(ServerLevel newLevel, double x, double y, double z, float yaw, float pitch, CallbackInfo ci, @Local(ordinal = 1) ServerLevel level) {
        ForgeEventFactory.firePlayerChangedDimensionEvent(this, level.dimension(), newLevel.dimension());
    }

    // Handled by Fabric API
    /*@Inject(method = "setRespawnPosition", at = @At("HEAD"), cancellable = true)
    private void kilt$checkPlayerSpawnSetEvent(ResourceKey<Level> dimension, @Nullable BlockPos position, float angle, boolean forced, boolean sendMessage, CallbackInfo ci) {
        if (ForgeEventFactory.onPlayerSpawnSet(this, dimension == null ? Level.OVERWORLD : dimension, position, forced))
            ci.cancel();
    }*/

    @Unique private String language = "en_us";

    @Override
    public String getLanguage() {
        return language;
    }

    @Unique private Component tabListHeader = Component.empty();
    @Unique private Component tabListFooter = Component.empty();

    @Override
    public Component getTabListHeader() {
        return tabListHeader;
    }

    @Override
    public Component getTabListFooter() {
        return tabListFooter;
    }

    @Override
    public void setTabListHeader(Component tabListHeader) {
        setTabListHeaderFooter(tabListHeader, this.tabListFooter);
    }

    @Override
    public void setTabListFooter(Component tabListFooter) {
        this.setTabListHeaderFooter(this.tabListHeader, tabListFooter);
    }

    @Override
    public void setTabListHeaderFooter(Component header, Component footer) {
        if (Objects.equals(header, this.tabListHeader) && Objects.equals(footer, this.tabListFooter))
            return;

        this.tabListHeader = Objects.requireNonNull(header, "header");
        this.tabListFooter = Objects.requireNonNull(footer, "footer");

        this.connection.send(new ClientboundTabListPacket(header, footer));
    }

    @Unique private boolean hasTabListName = false;
    @Unique private Component tabListDisplayName = null;

    @Override
    public void refreshTabListName() {
        var oldName = this.tabListDisplayName;
        this.tabListDisplayName = ForgeEventFactory.getPlayerTabListDisplayName(this);

        if (!Objects.equals(oldName, this.tabListDisplayName)) {
            this.getServer().getPlayerList().broadcastAll(new ClientboundPlayerInfoUpdatePacket(ClientboundPlayerInfoUpdatePacket.Action.UPDATE_DISPLAY_NAME, (ServerPlayer) (Object) this));
        }
    }

    @Inject(method = "drop(Z)Z", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Inventory;removeFromSelected(Z)Lnet/minecraft/world/item/ItemStack;"), cancellable = true)
    private void kilt$handleSelectedDrop(boolean dropStack, CallbackInfoReturnable<Boolean> cir, @Local Inventory inventory) {
        var selected = inventory.getSelected();

        if (selected.isEmpty() || !selected.onDroppedByPlayer(this)) {
            cir.setReturnValue(false);
            return;
        }

        // Forge: fix MC-231097 on the serverside
        if (this.isUsingItem() && this.getUsedItemHand() == InteractionHand.MAIN_HAND && (dropStack || selected.getCount() == 1))
            this.stopUsingItem();
    }

    @WrapOperation(method = "drop(Lnet/minecraft/world/item/ItemStack;ZZ)Lnet/minecraft/world/entity/item/ItemEntity;", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z"))
    private boolean kilt$captureDrops(Level instance, Entity entity, Operation<Boolean> original) {
        if (this.captureDrops() != null) {
            this.captureDrops().add((ItemEntity) entity);
            return false;
        }

        return original.call(instance, entity);
    }

    @WrapOperation(method = "drop(Z)Z", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;drop(Lnet/minecraft/world/item/ItemStack;ZZ)Lnet/minecraft/world/entity/item/ItemEntity;"))
    private ItemEntity kilt$callPlayerTossEvent(ServerPlayer instance, ItemStack droppedItem, boolean dropAround, boolean includeThrowerName, Operation<ItemEntity> original) {
        return ForgeHooks.kilt$onPlayerTossEvent(instance, droppedItem, includeThrowerName, () -> original.call(instance, droppedItem, dropAround, includeThrowerName));
    }
}