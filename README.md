# SciviewDM3D
Building a bridge between DM3D and Sciview

This is adapted from [minimal-sciview-example-project](https://github.com/scenerygraphics/minimal-sciview-example-project)
## Proof of concept

The first step is to visualize the meshes in sciview. A command plugin
has been created.

When sciview is running, load a volume and select the volume in the inspector. 
Then run the command, and select a .bmf file for the associated image.

The meshes will be loaded, and a JDialog will be created with a slider for
selecting the frame. It will set the frame for the volume and 
the meshes for the corresponding frame.


