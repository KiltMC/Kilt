/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforgespi;

import cpw.mods.modlauncher.api.IEnvironment;
import cpw.mods.modlauncher.api.ITransformationService;
import cpw.mods.modlauncher.api.TypesafeMap;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.progress.StartupNotificationManager;

import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Global environment variables - allows discoverability with other systems without full forge
 * dependency
 */
public class Environment {
    public static final class Keys {
        /**
         * The @{@link Dist} which is running.
         * Populated by forge during {@link ITransformationService#initialize(IEnvironment)}
         */
        public static final Supplier<TypesafeMap.Key<Dist>> DIST = IEnvironment.buildKey("FORGEDIST", Dist.class);
        /**
         * Provides a string consumer which can be used to push notification messages to the early startup GUI.
         */
        public static final Supplier<TypesafeMap.Key<Consumer<String>>> PROGRESSMESSAGE = IEnvironment.buildKey("PROGRESSMESSAGE", Consumer.class);
    }

    private static Environment INSTANCE;

    public static void build(IEnvironment environment) {
        INSTANCE = new Environment(environment);
        environment.computePropertyIfAbsent(Environment.Keys.PROGRESSMESSAGE.get(), v -> StartupNotificationManager.locatorConsumer().orElseGet(() -> s -> {}));
    }

    public static Environment get() {
        return INSTANCE;
    }

    private final Dist dist;

    private Environment(IEnvironment setup) {
        this.dist = setup.getProperty(Keys.DIST.get()).orElseThrow(() -> new RuntimeException("Missing DIST in environment!"));
    }

    public Dist getDist() {
        return this.dist;
    }
}
