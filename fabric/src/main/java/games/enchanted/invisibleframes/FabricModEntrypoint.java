package games.enchanted.invisibleframes;

import games.enchanted.invisibleframes.advancement.ModCriteriaTriggers;
import net.fabricmc.api.ModInitializer;

public class FabricModEntrypoint implements ModInitializer {
    @Override
    public void onInitialize() {
        InvisibleFramesMod.init();
        ModCriteriaTriggers.init();
    }
}
