package com.valterc.ki2.data.update;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Instant;

public class ReleaseInfoTest {

    private static ReleaseInfo releaseInfo(String name, int versionCode) {
        return new ReleaseInfo(name, "", Instant.EPOCH, "", "ki2-release.apk", "", 0, false, versionCode);
    }

    @Test
    public void newerReleaseIsAnUpdate() {
        Assertions.assertTrue(releaseInfo("16.0", 40).isUpdateFrom("15.0", 34));
        Assertions.assertTrue(releaseInfo("16.0-preview.1", 40).isUpdateFrom("15.0", 34));
        Assertions.assertTrue(releaseInfo("16.0", 41).isUpdateFrom("16.0-preview.1", 40));
    }

    @Test
    public void sameReleaseIsNotAnUpdate() {
        Assertions.assertFalse(releaseInfo("15.0", 34).isUpdateFrom("15.0", 34));
        Assertions.assertFalse(releaseInfo("16.0-preview.1", 40).isUpdateFrom("16.0-preview.1", 40));
    }

    @Test
    public void republishedOlderVersionIsAnUpdateToSupportDowngrade() {
        Assertions.assertTrue(releaseInfo("14.0", 40).isUpdateFrom("15.0", 34));
        Assertions.assertTrue(releaseInfo("16.0-preview.1", 41).isUpdateFrom("16.0-preview.2", 40));
    }

    @Test
    public void sameVersionCodeIsAnUpdateBecauseTheSystemAllowsIt() {
        Assertions.assertTrue(releaseInfo("14.0", 34).isUpdateFrom("15.0", 34));
    }

    @Test
    public void lowerVersionCodeIsNotAnUpdateBecauseTheSystemRejectsIt() {
        // Leaving the preview channel while the latest stable release is older than the installed
        // preview build. Offering it would fail to install and notify the user indefinitely.
        Assertions.assertFalse(releaseInfo("15.0", 34).isUpdateFrom("16.0-preview.2", 40));

        // A release removed from GitHub without being published again.
        Assertions.assertFalse(releaseInfo("14.0", 30).isUpdateFrom("15.0", 34));
    }

    @Test
    public void unknownVersionCodeIsAnUpdate() {
        Assertions.assertTrue(releaseInfo("14.0", ReleaseInfo.VERSION_CODE_UNKNOWN).isUpdateFrom("15.0", 34));
        Assertions.assertFalse(releaseInfo("15.0", ReleaseInfo.VERSION_CODE_UNKNOWN).isUpdateFrom("15.0", 34));
    }

}
