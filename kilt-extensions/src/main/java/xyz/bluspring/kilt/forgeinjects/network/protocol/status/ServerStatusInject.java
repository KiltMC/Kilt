package xyz.bluspring.kilt.forgeinjects.network.protocol.status;

import net.minecraft.network.protocol.status.ServerStatus;
import net.minecraftforge.network.ServerStatusPing;
import org.spongepowered.asm.mixin.Mixin;
import xyz.bluspring.kilt.injections.network.ServerStatusInjection;

import javax.annotation.Nullable;
import java.util.concurrent.Semaphore;

@Mixin(ServerStatus.class)
public class ServerStatusInject implements ServerStatusInjection {
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

    @Override
    public String getJson() {
        var ret = this.json;
        if (ret == null) {
            mutex.acquireUninterruptibly();
            ret = this.json;

            if (ret == null) {
                // TODO: more work is needed here
            }

            mutex.release();
        }

        return ret;
    }

    @Override
    public void invalidateJson() {
        this.json = null;
    }
}
