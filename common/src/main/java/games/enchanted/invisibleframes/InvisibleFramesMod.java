package games.enchanted.invisibleframes;

import games.enchanted.invisibleframes.platform.Services;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Items;

public class InvisibleFramesMod {

    public static void init() {
        InvisibleFramesLogging.info("Mod is loading in a {} environment!", Services.PLATFORM.getPlatformName());
    }
}