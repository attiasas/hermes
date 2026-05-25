package dev.hermes.tooling.android;

import java.io.File;

/**
 * Validates that a path looks like an Android SDK installation.
 */
public final class AndroidSdkValidator {

    private AndroidSdkValidator() {
    }

    public static boolean isValidSdk(File sdk) {
        if (sdk == null) {
            return false;
        }
        File platformTools = new File(sdk, "platform-tools");
        return sdk.isDirectory() && platformTools.isDirectory();
    }
}
