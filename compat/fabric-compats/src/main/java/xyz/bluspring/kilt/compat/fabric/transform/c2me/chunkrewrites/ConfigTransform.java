package xyz.bluspring.kilt.compat.fabric.transform.c2me.chunkrewrites;

import com.ishland.c2me.rewrites.chunksystem.common.Config;
import net.lenni0451.classtransform.annotations.CInline;
import net.lenni0451.classtransform.annotations.CSlice;
import net.lenni0451.classtransform.annotations.CTarget;
import net.lenni0451.classtransform.annotations.CTransformer;
import net.lenni0451.classtransform.annotations.injection.CModifyConstant;

@CTransformer(Config.class)
public class ConfigTransform {
    @CInline
    @CModifyConstant(method = "<clinit>", slice = @CSlice(from = @CTarget(value = "CONSTANT", target = "string chunkSystem.delayFullChunkEvents")), ordinal = 0, intValue = 0)
    private static int kilt$makeDelayFullChunkEventsDefault() {
        return 1;
    }
}
