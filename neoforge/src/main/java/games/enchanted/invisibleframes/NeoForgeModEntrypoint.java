package games.enchanted.invisibleframes;

import games.enchanted.invisibleframes.advancement.ModCriteriaTriggers;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.registries.RegisterEvent;

@Mod(InvisibleFramesConstants.MOD_ID)
public class NeoForgeModEntrypoint {
    public NeoForgeModEntrypoint(IEventBus bus) {
        InvisibleFramesMod.init();

        bus.addListener((RegisterEvent event) -> {
            if(event.getRegistry().key().equals(Registries.TRIGGER_TYPE)) {
                ModCriteriaTriggers.init();
            }
        });
    }
}