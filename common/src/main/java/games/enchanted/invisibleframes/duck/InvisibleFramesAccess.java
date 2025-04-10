package games.enchanted.invisibleframes.duck;

import java.util.ArrayList;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public interface InvisibleFramesAccess {
    ItemStack invisibleFrames$getInvisibleItem();
    ArrayList<ServerPlayer> invisibleFrames$getTrackedPlayers();
    void invisibleFrames$tickGhostManager();
}
