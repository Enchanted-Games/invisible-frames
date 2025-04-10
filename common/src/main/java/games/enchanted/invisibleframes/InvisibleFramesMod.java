package games.enchanted.invisibleframes;

import games.enchanted.invisibleframes.platform.Services;

public class InvisibleFramesMod {
    public static void init() {
        InvisibleFramesLogging.info("Mod is loading in a {} environment!", Services.PLATFORM.getPlatformName());
    }
}