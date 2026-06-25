package xyz.bluspring.kilt.injects.util;

import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.util.FileUtil;

@Mixin(FileUtil.class)
public abstract class FileUtilInject {
    //private static final Pattern RESERVED_WINDOWS_FILENAMES_NEOFORGE = Pattern.compile(".*\\.|(?:CON|PRN|AUX|NUL|CLOCK\\$|CONIN\\$|CONOUT\\$|(?:COM|LPT)[¹²³0-9])(?:\\..*)?", 2);
}
