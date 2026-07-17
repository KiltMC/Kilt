// TRACKED HASH: 8ce7cfcc1608a79d687631411c28c60d1064aad3
package xyz.bluspring.kilt.injects.server.level;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Cancellable;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundTabListPacket;
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
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.portal.DimensionTransition;
import net.neoforged.neoforge.common.CommonHooks;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.extensions.IBlockExtension;
import net.neoforged.neoforge.common.extensions.IEntityExtension;
import net.neoforged.neoforge.common.util.FriendlyByteBufUtil;
import net.neoforged.neoforge.entity.PartEntity;
import net.neoforged.neoforge.event.EventHooks;
import net.neoforged.neoforge.event.entity.player.PlayerContainerEvent;
import net.neoforged.neoforge.network.payload.AdvancedContainerSetDataPayload;
import net.neoforged.neoforge.network.payload.AdvancedOpenScreenPayload;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.bluspring.kilt.Kilt;
import xyz.bluspring.kilt.injections.server.level.ServerPlayerInjection;
import xyz.bluspring.kilt.injections.world.entity.player.PlayerInjection;
import xyz.bluspring.kilt.util.KiltHelper;

import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.function.Consumer;

@Mixin(value = ServerPlayer.class, priority = 1100)
public abstract class ServerPlayerInject extends Player implements ServerPlayerInjection {
    @Mixin(targets = "net.minecraft.server.level.ServerPlayer$0")
    public abstract static class AnonymousContainerSynchronizer0Inject {
        @Shadow
        @Final
        private ServerPlayer field_29182;

        @Inject(method = "broadcastDataValue", at = @At("HEAD"), cancellable = true)
        private void kilt$tryUseAdvancedContainerSetDataPayload(AbstractContainerMenu container, int id, int value, CallbackInfo ci) {
            // Kilt TODO: does this override anything of Fabric?
            if (field_29182.connection.hasChannel(AdvancedContainerSetDataPayload.TYPE)) {
                field_29182.connection.send(new AdvancedContainerSetDataPayload((byte) container.containerId, (short) id, value));
                ci.cancel();
            }
        }
    }

    @Shadow public ServerGamePacketListenerImpl connection;
    @Shadow @Final public ServerPlayerGameMode gameMode;
    @Shadow private @Nullable Entity camera;
    @Shadow public abstract ServerLevel serverLevel();

    public ServerPlayerInject(Level level, BlockPos pos, float yRot, GameProfile gameProfile) {
        super(level, pos, yRot, gameProfile);
    }

    @Inject(method = "doTick", at = @At(value = "FIELD", target = "Lnet/minecraft/server/level/ServerPlayer;tickCount:I", opcode = Opcodes.GETFIELD))
    private void kilt$resetFlyingAbility(CallbackInfo ci) { // Kilt: this seems like a bugfix...
        if (this.getAbilities().flying && !this.mayFly()) {
            this.getAbilities().flying = false;
            this.onUpdateAbilities();
        }
    }

    @Inject(method = "die", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;gameEvent(Lnet/minecraft/core/Holder;)V", shift = At.Shift.AFTER), cancellable = true)
    private void kilt$callForgeLivingDeath(DamageSource damageSource, CallbackInfo ci) {
        if (CommonHooks.onLivingDeath(this, damageSource))
            ci.cancel();
    }

    @WrapOperation(method = "findRespawnAndUseSpawnBlock", at = @At(value = "INVOKE", target = "Ljava/util/Optional;empty()Ljava/util/Optional;"))
    private static <T> Optional<T> kilt$tryFindRespawnPosition(Operation<Optional<T>> original, ServerLevel level, BlockPos pos, float angle, boolean forced, boolean keepInventory) {
        BlockState state = level.getBlockState(pos);

        if (KiltHelper.INSTANCE.hasMethodOverride(state.getBlock().getClass(), IBlockExtension.class, "getRespawnPosition", BlockState.class, EntityType.class, LevelReader.class, BlockPos.class, float.class)) {
            return (Optional<T>) state.getRespawnPosition(EntityType.PLAYER, level, pos, angle);
        } else {
            return original.call();
        }
    }

    @Inject(method = "changeDimension", at = @At("HEAD"), cancellable = true)
    private void kilt$callTravelToDimensionEventVanilla(DimensionTransition transition, CallbackInfoReturnable<Entity> cir) {
        if (!CommonHooks.onTravelToDimension(this, transition.newLevel().dimension()))
            cir.setReturnValue(null);
    }

    @WrapOperation(method = "changeDimension", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;unsetRemoved()V"))
    private void kilt$useReviveCallOnChangeDimensionVanilla(ServerPlayer instance, Operation<Void> original) {
        if (KiltHelper.INSTANCE.hasMethodOverride(instance.getClass(), IEntityExtension.class, "revive")) {
            this.revive();
        } else {
            original.call(instance);
        }
    }

    @Inject(method = "changeDimension", at = @At(value = "FIELD", target = "Lnet/minecraft/server/level/ServerPlayer;lastSentFood:I", shift = At.Shift.AFTER))
    private void kilt$firePlayerChangedDimensionEventVanilla(DimensionTransition transition, CallbackInfoReturnable<Entity> cir, @Local ResourceKey<Level> oldLevel) {
        EventHooks.firePlayerChangedDimensionEvent((ServerPlayer) (Object) this, oldLevel, transition.newLevel().dimension());
    }

    // Handled by Fabric API
    /*@Inject(at = @At("HEAD"), method = "startSleepInBed", cancellable = true)
    public void kilt$checkPlayerSleepEvent(BlockPos blockPos, CallbackInfoReturnable<Either<Player.BedSleepingProblem, Unit>> cir) {
        var ret = ForgeEventFactory.onPlayerSleepInBed((ServerPlayer) (Object) this, Optional.of(blockPos));
        if (ret != null)
            cir.setReturnValue(Either.left(ret));
    }*/

    @Inject(method = "bedInRange", at = @At("HEAD"), cancellable = true)
    private void kilt$checkIsBedInRange(BlockPos pos, Direction direction, CallbackInfoReturnable<Boolean> cir) {
        if (direction == null) // Kilt TODO: why is this needed?
            cir.setReturnValue(false);
    }

    @Unique private Consumer<RegistryFriendlyByteBuf> kilt$extraDataWriter = null;

    @Override
    public OptionalInt openMenu(MenuProvider menuProvider, Consumer<RegistryFriendlyByteBuf> extraDataWriter) {
        this.kilt$extraDataWriter = extraDataWriter;
        OptionalInt result = this.openMenu(menuProvider);
        this.kilt$extraDataWriter = null;

        return result;
    }

    @WrapOperation(method = "openMenu", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;closeContainer()V"))
    private void kilt$checkShouldTriggerClientSideClosing(ServerPlayer instance, Operation<Void> original, @Local(argsOnly = true) MenuProvider provider) {
        if (provider.shouldTriggerClientSideContainerClosingOnOpen()) {
            original.call(instance);
        } else {
            instance.doCloseContainer();
        }
    }

    @WrapOperation(method = "openMenu", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerGamePacketListenerImpl;send(Lnet/minecraft/network/protocol/Packet;)V"))
    private void kilt$sendAdditionalArbitraryData(ServerGamePacketListenerImpl instance, Packet<?> packet, Operation<Void> original, @Local AbstractContainerMenu menu, @Local(argsOnly = true) MenuProvider menuProvider) {
        Consumer<RegistryFriendlyByteBuf> extraDataWriter = this.kilt$extraDataWriter;

        var extraData = FriendlyByteBufUtil.writeCustomData(buffer -> {
            menuProvider.writeClientSideData(menu, buffer);
            if (extraDataWriter != null) {
                extraDataWriter.accept(buffer);
            }
        }, this.registryAccess());

        if (extraData.length != 0) {
            instance.send(new AdvancedOpenScreenPayload(menu.containerId, menu.getType(), menuProvider.getDisplayName(), extraData));
        } else {
            original.call(instance, packet);
        }
    }

    @Inject(method = "openMenu", at = @At(value = "INVOKE", target = "Ljava/util/OptionalInt;of(I)Ljava/util/OptionalInt;"))
    private void kilt$callContainerOpenEvent(MenuProvider menu, CallbackInfoReturnable<OptionalInt> cir) {
        NeoForge.EVENT_BUS.post(new PlayerContainerEvent.Open(this, this.containerMenu));
    }

    @Inject(method = "openHorseInventory", at = @At("TAIL"))
    private void kilt$callContainerOpenEvent(AbstractHorse horse, Container inventory, CallbackInfo ci) {
        NeoForge.EVENT_BUS.post(new PlayerContainerEvent.Open(this, this.containerMenu));
    }

    @Inject(method = "doCloseContainer", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/inventory/InventoryMenu;transferState(Lnet/minecraft/world/inventory/AbstractContainerMenu;)V", shift = At.Shift.AFTER))
    private void kilt$callContainerCloseEvent(CallbackInfo ci) {
        NeoForge.EVENT_BUS.post(new PlayerContainerEvent.Close(this, this.containerMenu));
    }

    @Inject(method = "restoreFrom", at = @At("TAIL"))
    private void kilt$storeTabListData(ServerPlayer that, boolean keepEverything, CallbackInfo ci) {
        CompoundTag old = that.getPersistentData();
        if (old.contains(PlayerInjection.PERSISTED_NBT_TAG)) {
            this.getPersistentData().put(PlayerInjection.PERSISTED_NBT_TAG, old.get(PlayerInjection.PERSISTED_NBT_TAG));
        }

        EventHooks.onPlayerClone(this, that, !keepEverything);
        this.tabListHeader = that.getTabListHeader();
        this.tabListFooter = that.getTabListFooter();
    }

    @ModifyVariable(method = "setGameMode", at = @At("HEAD"), argsOnly = true)
    private GameType kilt$callChangeGameType(GameType value, @Cancellable CallbackInfoReturnable<Boolean> cir) {
        value = CommonHooks.onChangeGameType(this, this.gameMode.getGameModeForPlayer(), value);

        if (value == null) {
            cir.setReturnValue(false);
            return null;
        }

        return value;
    }

    @Inject(method = "setCamera", at = @At(value = "FIELD", target = "Lnet/minecraft/server/level/ServerPlayer;camera:Lnet/minecraft/world/entity/Entity;", ordinal = 0))
    private void kilt$usePartEntityParentCamera(Entity entityToSpectate, CallbackInfo ci) {
        while (this.camera instanceof PartEntity<?> partEntity)
            this.camera = partEntity.getParent();
    }

    @ModifyReturnValue(method = "getTabListDisplayName", at = @At("RETURN"))
    private Component kilt$addTabListDisplayName(Component original) {
        if (!this.hasTabListName) {
            this.tabListDisplayName = EventHooks.getPlayerTabListDisplayName(this);
            this.hasTabListName = true;
        }

        if (this.tabListDisplayName == null)
            return original;

        return this.tabListDisplayName;
    }

    // Handled by Fabric API
    /*@Inject(method = "setRespawnPosition", at = @At("HEAD"), cancellable = true)
    private void kilt$checkPlayerSpawnSetEvent(ResourceKey<Level> dimension, @Nullable BlockPos position, float angle, boolean forced, boolean sendMessage, CallbackInfo ci) {
        if (EventHooks.onPlayerSpawnSet(this, dimension == null ? Level.OVERWORLD : dimension, position, forced))
            ci.cancel();
    }*/

    @Shadow private String language;

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
        this.tabListDisplayName = EventHooks.getPlayerTabListDisplayName(this);

        if (!Objects.equals(oldName, this.tabListDisplayName)) {
            this.getServer().getPlayerList().broadcastAll(new ClientboundPlayerInfoUpdatePacket(ClientboundPlayerInfoUpdatePacket.Action.UPDATE_DISPLAY_NAME, (ServerPlayer) (Object) this));
        }
    }

    @Inject(method = "drop(Z)Z", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Inventory;removeFromSelected(Z)Lnet/minecraft/world/item/ItemStack;"), cancellable = true)
    private void kilt$handleSelectedDrop(boolean dropStack, CallbackInfoReturnable<Boolean> cir, @Local Inventory inventory, @Share(value = "selected", namespace = Kilt.MOD_ID) LocalRef<ItemStack> selectedRef) {
        var selected = inventory.getSelected();
        selectedRef.set(selected);

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
        return CommonHooks.kilt$onPlayerTossEvent(instance, () -> original.call(instance, droppedItem, dropAround, includeThrowerName));
    }
}
