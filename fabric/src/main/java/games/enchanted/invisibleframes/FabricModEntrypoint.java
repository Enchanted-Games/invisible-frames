package games.enchanted.invisibleframes;

import net.fabricmc.api.ModInitializer;

public class FabricModEntrypoint implements ModInitializer {
    @Override
    public void onInitialize() {
        InvisibleFramesMod.init();
    }
}
