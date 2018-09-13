# Claudy
Minecraft mod to backup and restore chunks of terrain on-the-fly.  
Saves blocks and tile entities (chests, signs, etc.).

Compatible with **Minecraft 1.12.2**

---

### Usage
```
/claudy save <label> <x1> <y1> <z1> <x2> <y2> <z2>
/claudy <restore|delete> <label>
```
with (x1, y1, z1) and (x2, y2, z2) the two foremost edges of the cube to be saved

### Use cases
+ You want to backup a work in progress to be able to undo changes
+ An area gets repeatedly destroyed and you want to avoid the tedious task of rebuilding it every time
+ You want to keep track of what is happening blockwise (who builds/destroys blocks, how many, where, etc.)
+ You want to troll people