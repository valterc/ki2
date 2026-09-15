package com.valterc.ki2.data.update;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Instant;

public class ReleaseInfoTest {

    private static ReleaseInfo releaseInfo(String name) {
        return new ReleaseInfo(name, "", Instant.EPOCH, "", "ki2-release.apk", "", 0, false);
    }

    @Test
    public void newerReleaseIsAnUpdate() {
        Assertions.assertTrue(releaseInfo("16.0").isUpdateFrom("15.0"));
        Assertions.assertTrue(releaseInfo("16.0-preview.1").isUpdateFrom("15.0"));
        Assertions.assertTrue(releaseInfo("16.0").isUpdateFrom("16.0-preview.1"));
    }

    @Test
    public void sameReleaseIsNotAnUpdate() {
        Assertions.assertFalse(releaseInfo("15.0").isUpdateFrom("15.0"));
        Assertions.assertFalse(releaseInfo("16.0-preview.1").isUpdateFrom("16.0-preview.1"));
    }

    @Test
    public void olderReleaseIsAnUpdateToSupportDowngrade() {
        Assertions.assertTrue(releaseInfo("14.0").isUpdateFrom("15.0"));
        Assertions.assertTrue(releaseInfo("16.0-preview.1").isUpdateFrom("16.0-preview.2"));
        Assertions.assertTrue(releaseInfo("16.0-preview.1").isUpdateFrom("16.0"));
    }

}
