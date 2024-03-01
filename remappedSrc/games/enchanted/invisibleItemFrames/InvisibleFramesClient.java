package games.enchanted.invisibleItemFrames;

import net.fabricmc.api.ClientModInitializer;

public class InvisibleFramesClient implements ClientModInitializer {

  @Override
  public void onInitializeClient() {
		InvisibleFrames.log.info(InvisibleFrames.MOD_ID + " is installed on the client. It will only work in singleplayer worlds or servers that also have the mod installed.");
  }
  
}
