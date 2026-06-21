//? if neoforge {
/*package games.enchanted.eg_invisible_frames.neoforge;

import games.enchanted.eg_invisible_frames.common.ModConstants;
import games.enchanted.eg_invisible_frames.common.ModEntry;
import games.enchanted.eg_invisible_frames.common.advancement.ModCriteriaTriggers;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.registries.RegisterEvent;

/^*
 * This is the entry point for your mod's neoforge side.
 ^/
@Mod(ModConstants.MOD_ID)
public class NeoForgeEntry {
    public NeoForgeEntry(IEventBus bus) {
        ModEntry.init();

        bus.addListener((RegisterEvent event) -> {
            if(event.getRegistry().key().equals(Registries.TRIGGER_TYPE)) {
                ModCriteriaTriggers.init();
            }
        });
    }
}
*///?}