/*
 * Copyright (c) NeoForged and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforgespi.locating;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.neoforgespi.language.IModInfo;
import org.apache.maven.artifact.versioning.ArtifactVersion;
import org.apache.maven.artifact.versioning.DefaultArtifactVersion;
import org.apache.maven.artifact.versioning.InvalidVersionSpecificationException;
import org.apache.maven.artifact.versioning.VersionRange;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

/**
 * ForgeFeature is a simple test for mods for the presence of specific features
 * such as OpenGL of a specific version or better or whatever.
 *
 * {@snippet :
 * ForgeFeature.registerFeature("openGLVersion", VersionFeatureTest.forVersionString(IModInfo.DependencySide.CLIENT, "3.2"));
 * }
 *
 * This will be tested during early mod loading against lists of features in the mods.toml file for mods. Those
 * that are absent or out of range will be rejected.
 */
public class ForgeFeature {
    private ForgeFeature() {}

    private static final Map<String, IFeatureTest<?>> features = new HashMap<>();

    public static <T> void registerFeature(final String featureName, final IFeatureTest<T> featureTest) {
        features.put(featureName, featureTest);
    }

    private static final MissingFeatureTest MISSING = new MissingFeatureTest();

    public static boolean testFeature(final Dist side, final Bound bound) {
        return features.getOrDefault(bound.featureName(), MISSING).testSideWithString(side, bound.featureBound());
    }

    public static Object featureValue(final Bound bound) {
        return features.getOrDefault(bound.featureName(), MISSING).featureValue();
    }

    public sealed interface IFeatureTest<F> extends Predicate<F> {
        IModInfo.DependencySide applicableSides();

        F convertFromString(final String value);

        String featureValue();

        default boolean testSideWithString(final Dist side, final String value) {
            return !applicableSides().isContained(side) || test(convertFromString(value));
        }
    }

    /**
     * A Bound, from a mods.toml file
     *
     * @param featureName  the name of the feature
     * @param featureBound the requested bound
     */
    public record Bound(String featureName, String featureBound, IModInfo modInfo) {
        @SuppressWarnings("unchecked")
        public <T> T bound() {
            return (T) features.getOrDefault(featureName, MISSING).convertFromString(featureBound);
        }
    }

    /**
     * Version based feature test. Uses standard MavenVersion system. Will test the constructed version against
     * ranges requested by mods.
     * 
     * @param version The version we wish to test against
     */
    public record VersionFeatureTest(IModInfo.DependencySide applicableSides, ArtifactVersion version) implements IFeatureTest<VersionRange> {
        /**
         * Convenience method for constructing the feature test for a version string
         * 
         * @param version the string
         * @return the feature test for the supplied string
         */
        public static VersionFeatureTest forVersionString(final IModInfo.DependencySide side, final String version) {
            return new VersionFeatureTest(side, new DefaultArtifactVersion(version));
        }

        @Override
        public String featureValue() {
            return version.toString();
        }

        @Override
        public boolean test(final VersionRange versionRange) {
            return versionRange.containsVersion(version);
        }

        @Override
        public VersionRange convertFromString(final String value) {
            try {
                return VersionRange.createFromVersionSpec(value);
            } catch (InvalidVersionSpecificationException e) {
                throw new IllegalArgumentException(e);
            }
        }
    }

    public record BooleanFeatureTest(IModInfo.DependencySide applicableSides, boolean value) implements IFeatureTest<Boolean> {
        @Override
        public boolean test(final Boolean aBoolean) {
            return aBoolean.equals(value);
        }

        @Override
        public String featureValue() {
            return Boolean.toString(value);
        }

        @Override
        public Boolean convertFromString(final String value) {
            return Boolean.parseBoolean(value);
        }
    }

    private record MissingFeatureTest() implements IFeatureTest<Object> {
        @Override
        public IModInfo.DependencySide applicableSides() {
            return IModInfo.DependencySide.BOTH;
        }

        @Override
        public String featureValue() {
            return "NONE";
        }

        @Override
        public boolean test(final Object o) {
            return false;
        }

        @Override
        public Object convertFromString(final String value) {
            return null;
        }
    }
}
