# LT 3D Importer
An addon for [Little Tiles Mod](https://www.curseforge.com/minecraft/mc-mods/littletiles) that makes building even more exciting!
This mod allows you to import a 3d model (currently only in .obj format) as a Little Tiles structure
Unbelievably, it requires LittleTiles as well as CreativeCore mods to run. Go find them on CurseForge if you don't have them yet.

The only limit with this mod is your computer (and Minecraft)

## But.. HOW?

![Converted](https://i.imgur.com/7x1chXW.png)

In a creative inventory, find LittleTiles tab and find new premade structure (currently with a weird name) that should contain word "modelimporter". It should look like a block next to the dog on the picture above. Place it down and click on it - a pretty self explanatory GUI will open. Every text field has a tooltip to understand what it does.

- You can either use a texture or go with custom color

- Base block can also be set to whatever block you like - default is plain LittleTiles Colored Block

- Grid will determine the size of grid to be used

- Precision text box is an advanced parameter that can help smooth the converted structure. Lower values mean higher precision. It is not recommended to set values above `0.9` as that can cause little holes in the structure and values below `0.01` have no noticeable effect and can extend the time needed to generate the structure multiple times

- Max size is the maximum length/height or thickness of the structure, whichever has the highest value. This value is in LittleTiles not vanilla Minecraft blocks. For example a dog model with grid `16` and max size `256` (which is `16*16`) will never be longer/thicker or taller than 16 vanilla blocks.

 

It is also required to have an empty Advanced Recipe in the output slot (next to the Convert button) if working in survival mode. However, that shouldn't be a problem since the model importer doesn't have a recipe yet.

Proper wiki is coming soon.

Also, please be reasonable and do not import models with large max size numbers (above 1000), it can take up to an hour to place it, depending on your PC.

## Releases
GitHub - https://github.com/Timardo/LT-3D-Importer/releases

CurseForge - https://www.curseforge.com/minecraft/mc-mods/littletiles-3d-importer/files

## TODO

An actual GUI with options:
###
 - ~~full path name to model~~ [Done]
 - ~~full path name to texture (optional)~~ [Done]
 - ~~select a block which will be used as a base~~ [Done]
 - ~~select a color for the model (optional)~~ [Done]
 - resize the model (and see its actual dimensions)
###
 - Preview the model in its full 3d glory, somehow?

 - Make the importing server friendly

 - Some multithreading?

 - Ability to rotate the model in a preview state?

 - Minimize tiles used? (remove those players cannot actually see)
 
 - Parse .mtl files
 
 - Fix localizing issue
 
 - Fix recipe issue

 - ~~Make it faster~~ [Done]

 - ~~Make min. precision configurable~~ [Done]

Kudos to Fredlllll for [his plugin](https://dev.bukkit.org/projects/print3d). It would take me much more time figuring out how to work
with textures without it.

This mod uses [Obj lib](https://github.com/javagl/Obj) for loading obj files.
