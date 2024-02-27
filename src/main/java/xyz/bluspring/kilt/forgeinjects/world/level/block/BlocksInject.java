// TRACKED HASH: d2c363dd841c4bf27dd0d480abbc0a106d8e20ce
package xyz.bluspring.kilt.forgeinjects.world.level.block;

import net.minecraft.world.level.block.Blocks;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Blocks.class)
public class BlocksInject {
    // oh sweet dear jebediah and of all jesus with christ, what in the fuck
    // i hope to god this doesn't break anything in fabric, because if it does,
    // we need to find a way that doesn't use this.

    // ignore what i said above, literally nothing works if i do this
    /*@Redirect(at = @At(value = "INVOKE", target = "Ljava/util/Iterator;hasNext()Z"), method = "<clinit>")
    private static boolean kilt$disableIdMapCaching(Iterator instance) {
        return false;
    }*/
}