package xyz.bluspring.kilt.forgeinjects.network.protocol.status;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.network.protocol.status.ClientboundStatusResponsePacket;
import net.minecraft.network.protocol.status.ServerStatus;
import net.minecraft.util.GsonHelper;
import net.minecraftforge.network.ServerStatusPing;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import xyz.bluspring.kilt.injections.network.ServerStatusInjection;

import javax.annotation.Nullable;
import java.lang.reflect.Type;
import java.util.concurrent.Semaphore;

@Mixin(ServerStatus.class)
public class ServerStatusInject implements ServerStatusInjection {
    @Unique
    private transient ServerStatusPing forgeData;

    @Override
    @Nullable
    public ServerStatusPing getForgeData() {
        return forgeData;
    }

    private Semaphore mutex = new Semaphore(1);
    private String json = null;

    @Override
    public void setForgeData(ServerStatusPing data) {
        forgeData = data;
        invalidateJson();
    }

    @Inject(at = @At("TAIL"), method = {"setDescription", "setFavicon", "setEnforcesSecureChat", "setPreviewsChat", "setPlayers", "setVersion"})
    public void kilt$invalidateJsonData(CallbackInfo ci) {
        invalidateJson();
    }

    @Override
    public String getJson() {
        var ret = this.json;
        if (ret == null) {
            mutex.acquireUninterruptibly();
            ret = this.json;

            if (ret == null) {
                ret = ClientboundStatusResponsePacket.GSON.toJson(this);
                this.json = ret;
            }

            mutex.release();
        }

        return ret;
    }

    @Override
    public void invalidateJson() {
        this.json = null;
    }

    @Mixin(ServerStatus.Serializer.class)
    public static class SerializerInject {
        @Inject(method = "deserialize(Lcom/google/gson/JsonElement;Ljava/lang/reflect/Type;Lcom/google/gson/JsonDeserializationContext;)Lnet/minecraft/network/protocol/status/ServerStatus;", at = @At("TAIL"))
        private void kilt$loadForgeData(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext, CallbackInfoReturnable<ServerStatus> cir, @Local JsonObject json, @Local ServerStatus status) {
            if (json.has("forgeData")) {
                status.setForgeData(ServerStatusPing.Serializer.deserialize(GsonHelper.getAsJsonObject(json, "forgeData")));
            }
        }

        @Inject(method = "serialize(Lnet/minecraft/network/protocol/status/ServerStatus;Ljava/lang/reflect/Type;Lcom/google/gson/JsonSerializationContext;)Lcom/google/gson/JsonElement;", at = @At("TAIL"))
        private void kilt$saveForgeData(ServerStatus serverStatus, Type type, JsonSerializationContext jsonSerializationContext, CallbackInfoReturnable<JsonElement> cir, @Local JsonObject json) {
            if (serverStatus.getForgeData() != null) {
                json.add("forgeData", ServerStatusPing.Serializer.serialize(serverStatus.getForgeData()));
            }
        }
    }
}
