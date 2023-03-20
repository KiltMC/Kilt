package xyz.bluspring.kilt.forgeinjects.client.multiplayer;

import net.minecraft.client.multiplayer.ServerData;
import net.minecraftforge.client.ExtendedServerListData;
import org.spongepowered.asm.mixin.Mixin;
import xyz.bluspring.kilt.injections.client.multiplayer.ServerDataInjection;

@Mixin(ServerData.class)
public class ServerDataInject implements ServerDataInjection {
    public ExtendedServerListData forgeData = null;

    @Override
    public ExtendedServerListData getForgeData() {
        return forgeData;
    }

    @Override
    public void setForgeData(ExtendedServerListData data) {
        forgeData = data;
    }
}
