package xyz.bluspring.kilt.injects;

import net.minecraft.FileUtil;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(FileUtil.class)
public abstract class FileUtilInject {
    //private static final Pattern RESERVED_WINDOWS_FILENAMES_NEOFORGE = Pattern.compile(".*\\.|(?:CON|PRN|AUX|NUL|CLOCK\\$|CONIN\\$|CONOUT\\$|(?:COM|LPT)[¹²³0-9])(?:\\..*)?", 2);
}
