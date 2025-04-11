# Invisible Frames
A survival-friendly way to make invisible item frames!

To make an item frame invisible, make sure there is an item in the frame and right-click it with any glass pane. To make the item frame visible again, sneak and left-click it or left-click it twice while not sneaking!

This mod is fully server-side which means it only needs to be installed on the server. Any client (even vanilla) can join and make item frames invisible!

<details>
<summary>Versions before v2.0</summary>
To make item frames invisible in v1.3 or below, simply left-click the item frame while holding a glass pane.
</details>

## Features
- Invisible item frames made with the mod will emit a glowing white particle when they are not holding an item
- Adds an advancement "Where did it go?" that is granted when an item frame is made invisible
- Items that can make an item frame invisible can be changed with a datapack
- The item that was used to make the item frame invisible is dropped when the item frane is made visible again

## Customisation
Items that can make Item Frames invisible are controlled by an item tag, which is basically just a list of items. You can find an example datapack in the [GitHub Releases page](https://github.com/Enchanted-Games/invisible-frames/releases) for each mod version (as of v2.0).

To add items to this tag, you can make a datapack and add a file at `data/eg-invisible-frames/tags/items/makes_item_frames_invisible.json`. See the [default one here](https://github.com/Enchanted-Games/invisible-frames/tree/main/src/main/resources/data/eg-invisible-frames/tags/items) or [read more about tags](https://minecraft.wiki/w/Tag#JSON_format).

By default, this contains `minecraft:glass_pane` and `#c:glass_panes` (this is a [Fabric Conventional Tag](https://fabricmc.net/wiki/community:common_tags) that includes vanilla and most modded glass panes).

## License

<p xmlns:cc="http://creativecommons.org/ns#" >Invisible Frames by <a rel="cc:attributionURL dct:creator" property="cc:attributionName" href="https://enchanted.games">ioblackshaw (a.k.a. Enchanted_Games)</a> is licensed under <a href="http://creativecommons.org/licenses/by-nc/4.0/?ref=chooser-v1" target="_blank" rel="license noopener noreferrer" style="display:inline-block;">CC BY-NC 4.0<img style="height:22px!important;margin-left:3px;vertical-align:text-bottom;" src="https://mirrors.creativecommons.org/presskit/icons/cc.svg?ref=chooser-v1"><img style="height:22px!important;margin-left:3px;vertical-align:text-bottom;" src="https://mirrors.creativecommons.org/presskit/icons/by.svg?ref=chooser-v1"><img style="height:22px!important;margin-left:3px;vertical-align:text-bottom;" src="https://mirrors.creativecommons.org/presskit/icons/nc.svg?ref=chooser-v1"></a></p>
Video content creators may monetise videos including this work provided the license is followed.
