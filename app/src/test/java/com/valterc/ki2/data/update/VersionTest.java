package com.valterc.ki2.data.update;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class VersionTest {

    @Test
    public void parseStableVersion() {
        Version version = Version.parse("15.0");
        Assertions.assertNotNull(version);
        Assertions.assertFalse(version.isPreview());
        Assertions.assertEquals("15.0", version.toString());
    }

    @Test
    public void parsePreviewVersion() {
        Version version = Version.parse("16.0-preview.1");
        Assertions.assertNotNull(version);
        Assertions.assertTrue(version.isPreview());
        Assertions.assertEquals("16.0-preview.1", version.toString());
    }

    @Test
    public void parsePreviewVersionWithoutNumber() {
        Version version = Version.parse("16.0-preview");
        Assertions.assertNotNull(version);
        Assertions.assertTrue(version.isPreview());
    }

    @Test
    public void parseInvalidVersion() {
        Assertions.assertNull(Version.parse(null));
        Assertions.assertNull(Version.parse(""));
        Assertions.assertNull(Version.parse("latest"));
        Assertions.assertNull(Version.parse("16.0-rc.1"));
        Assertions.assertNull(Version.parse("16.0-preview.1-extra"));
    }

    @Test
    public void isPreviewIdentifiesPreviewVersions() {
        Assertions.assertTrue(Version.isPreview("16.0-preview.1"));
        Assertions.assertTrue(Version.isPreview("16.0-PREVIEW.1"));
        Assertions.assertFalse(Version.isPreview("16.0"));
        Assertions.assertFalse(Version.isPreview("not-a-version"));
    }

}
