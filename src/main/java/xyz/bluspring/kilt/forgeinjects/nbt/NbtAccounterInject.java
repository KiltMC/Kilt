package xyz.bluspring.kilt.forgeinjects.nbt;

import net.minecraft.nbt.NbtAccounter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import xyz.bluspring.kilt.injections.nbt.NbtAccounterInjection;

@Mixin(NbtAccounter.class)
public abstract class NbtAccounterInject implements NbtAccounterInjection {
    @Shadow public abstract void accountBytes(long bytes);

    /*
     * UTF8 is not a simple encoding system, each character can be either
     * 1, 2, or 3 bytes. Depending on where it's numerical value falls.
     * We have to count up each character individually to see the true
     * length of the data.
     *
     * Basic concept is that it uses the MSB of each byte as a 'read more' signal.
     * So it has to shift each 7-bit segment.
     *
     * This will accurately count the correct byte length to encode this string, plus the 2 bytes for its length prefix.
     */
    @Override
    public String readUTF(String data) {
        accountBytes(2); // Header length

        if (data == null)
            return null;

        int len = data.length();
        int utfLen = 0;

        for (int i = 0; i < len; i++) {
            int c = data.charAt(i);

            if ((c >= 0x0001) && (c <= 0x007F))
                utfLen += 1;
            else if (c > 0x07FF)
                utfLen += 3;
            else
                utfLen += 2;
        }

        accountBytes(utfLen);
        return data;
    }
}
