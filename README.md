# LT 3D Importer
An addon for [Little Tiles Mod](https://www.curseforge.com/minecraft/mc-mods/littletiles) that makes building even more exciting!
This mod allows you to import a 3d model (currently only in .obj format) as a Little Tiles structure
Unbelievably, it requires LittleTiles as well as CreativeCore mods to run. Go find them on CurseForge if you don't have them yet.

The only limit with this mod is your computer (and Minecraft)

## But.. HOW?

This is a very early version which only works through a command: `/3dconvert <3dmodel_file> <max_size> <grid_size> <precision> [texture_file] [color]`
Note that the 3d model file has to be in the base minecraft folder (where the mods folder is) and MUST contain the file extension
(.obj most of the times). Max size parameter is the max size in any direction of your imported 3d model
(so it will NEVER be longer/higher/thiccer than this value), this value is in little tile blocks. Grid size accepts any available grid
size specified in the config, otherwise, the game will nicely tell you (in red text) to use a valid size. Precision is a number that affects
the precision of converting polygons to blocks. Lower values mean more precise conversion and slower conversion. Value 0.7 is recommended
minimum and value 0.01 is recommended maximum. Values below this threshold will not have noticeable effect. Texture file and color is an
optional parameter (only one at a time is valid). Texture file also must lie in the base minecraft folder and contain the file extension.
Color is defined by 3 integers in range 0 - 255 each setting the value of red, green and blue in the final color. If there is no color
or texture defined, all little tiles will be white.

Examples:

`/3dconvert model.obj 512 32 0.7 texture.png` - this will load file model.obj in the base minecraft folder with grid size of
32, max size of 512 little tiles (so the model will be at most 16 minecraft blocks in any dimension), precision 0.7 and texture in file texture.png

`/3dconvert 3dobject.obj 100 16 0.4 127 0 255` - this one will not load a texture but instead paint all blocks with specific color
R: 127 G: 0 B: 255

`/3dconvert cocacolaglass.obj 100 10 0.1` - this will throw an error because 10 is not (by default) a valid grid size

No, the mod will not work through commands at its final release

Also, please be reasonable and do not import models with large max_size numbers (above 1000), it can take as much as ~~20 minutes~~ 
1 second  to generate the output advanced recipe and up to an hour to actually place it, depending on your PC.

## Releases
GitHub - https://github.com/Timardo/LT-3D-Importer/releases

CurseForge - soon

## TODO

An actual GUI with options:
###
 - full path name to model
 - full path name to texture (optional)
 - select a block which will be used as a base
 - select a color for the model (optional)
 - resize the model (and see its actual dimensions)
###
 - Preview the model in its full 3d glory, somehow?

 - Make the importing server friendly

 - Some multithreading?

 - Ability to rotate the model in a preview state?

 - Minimize tiles used? (remove those players cannot actually see)
 
 - Parse .mtl files

 - ~~Make it faster~~ [Done]

 - ~~Make min. precision configurable~~ [Done]

Kudos to Fredlllll for [his plugin](https://dev.bukkit.org/projects/print3d). It would take me much more time figuring out how to work
with textures without it.

This mod uses [Obj lib](https://github.com/javagl/Obj) for loading obj files.
