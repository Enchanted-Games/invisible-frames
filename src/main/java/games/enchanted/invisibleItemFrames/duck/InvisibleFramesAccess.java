package games.enchanted.invisibleItemFrames.duck;

import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.ArrayList;

public interface InvisibleFramesAccess {
    ItemStack invisible_frames$getGlassPaneStack();
    ArrayList<ServerPlayerEntity> invisible_frames$getTrackedPlayers();
}
