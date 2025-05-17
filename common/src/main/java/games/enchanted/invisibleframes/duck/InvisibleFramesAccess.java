package games.enchanted.invisibleframes.duck;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;

public interface InvisibleFramesAccess {
    ItemStack invisibleFrames$getInvisibleItem();

    ArrayList<ServerPlayer> invisibleFrames$getTrackedPlayers();
    void invisibleFrames$startSeenByPlayer(ServerPlayer player);
    void invisibleFrames$stopSeenByPlayer(ServerPlayer player);

    void invisibleFrames$tickGhostManager();
    void invisibleFrames$resetGhostAnimation();
}
