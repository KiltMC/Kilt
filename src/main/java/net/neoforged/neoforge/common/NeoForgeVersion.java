package net.neoforged.neoforge.common;

import org.jetbrains.annotations.ApiStatus;
import xyz.bluspring.kilt.loader.Constants;

@ApiStatus.Internal
public final class NeoForgeVersion {
    private NeoForgeVersion() {}

    private static final NeoForgeBuildType BUILD_TYPE = NeoForgeBuildType.STABLE;

    public static String getVersion() {
        return Constants.NEOFORGE_API_VERSION.toString();
    }

    public static NeoForgeBuildType getBuildType() {
        return BUILD_TYPE;
    }
}

