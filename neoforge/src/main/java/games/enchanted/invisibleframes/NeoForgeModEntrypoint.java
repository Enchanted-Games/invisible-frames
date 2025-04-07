package games.enchanted.invisibleframes;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

@Mod(InvisibleFramesConstants.MOD_ID)
public class NeoForgeModEntrypoint {
    public NeoForgeModEntrypoint(IEventBus eventBus) {
        InvisibleFramesMod.init();
    }
}