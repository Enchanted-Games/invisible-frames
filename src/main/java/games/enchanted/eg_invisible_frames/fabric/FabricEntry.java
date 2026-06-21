//? if fabric {
package games.enchanted.eg_invisible_frames.fabric;

import games.enchanted.eg_invisible_frames.common.ModEntry;
import games.enchanted.eg_invisible_frames.common.advancement.ModCriteriaTriggers;
import net.fabricmc.api.ModInitializer;

public class FabricEntry implements ModInitializer {
    @Override
    public void onInitialize() {
        ModEntry.init();
        ModCriteriaTriggers.init();
    }
}
//?}