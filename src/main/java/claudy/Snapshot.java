package claudy;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagShort;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

public class Snapshot
{
    public static class Box
    {
        int minX;
        int minY;
        int minZ;
        int maxX;
        int maxY;
        int maxZ;

        public Box(int minX, int minY, int minZ, int maxX, int maxY, int maxZ)
        {
            this.minX = minX;
            this.minY = minY;
            this.minZ = minZ;
            this.maxX = maxX;
            this.maxY = maxY;
            this.maxZ = maxZ;
        }
    }

    public static void save(String label, Box box, World world)
    {
        saveSnapshot(label + ".x", box, world);
    }

    public static void restore(String label, World world)
    {
        // Load NBT from file
        NBTTagCompound mainCompound = loadSnapshot(label + ".x");

        // Extract headers
        int[] minVertex = mainCompound.getIntArray("minVertex");
        int[] maxVertex = mainCompound.getIntArray("maxVertex");
        Box box = new Box(minVertex[0], minVertex[1], minVertex[2], maxVertex[0], maxVertex[1], maxVertex[2]);

        // TODO: option for choosing between blocks, tile entities or both

        // Extract blocks and process them
        NBTTagList NBTBlocks = mainCompound.getTagList("blocks", new NBTTagShort().getId());
        NBTTagList NBTTileEntities = mainCompound.getTagList("tile_entities", new NBTTagCompound().getId());
        processBlocks(box, NBTBlocks, NBTTileEntities, world);

        // TODO: Extract tile entities and process them
        // processTileEntities(box, NBTTileEntities, world);
    }

    private static void processBlocks(Box box, NBTTagList NBTBlocks, NBTTagList NBTTileEntities, World world)
    {
        // Iterate blocks within bounding box and currently loaded blocks
        Iterator<NBTBase> blockIterator = NBTBlocks.iterator();
        Iterator<NBTBase> tileEntitiesIterator = NBTTileEntities.iterator();

        for (int x = (int) box.minX; x <= (int) box.maxX; x++) {
            for (int y = (int) box.minY; y <= (int) box.maxY; y++) {
                for (int z = (int) box.minZ; z <= (int) box.maxZ; z++) {
                    BlockPos pos = new BlockPos(x, y, z);

                    // Generate block state from snapshot
                    short payload = ((NBTTagShort) blockIterator.next()).getShort();
                    int blockID = payload >> 8;
                    int metadata = payload & 0xFF;
                    Block block = Block.getBlockById(blockID);
                    IBlockState blockState = block.getStateFromMeta(metadata);
                    
                    world.setBlockState(pos, blockState, 2);
                    
                    if (block.hasTileEntity(null)) {
                        NBTTagCompound NBTTileEntity = (NBTTagCompound) tileEntitiesIterator.next();
                        TileEntity tileEntity = TileEntity.create(world, NBTTileEntity);
                        System.out.println(blockState);
                        System.out.println(tileEntity);
                        System.out.println();
                        if (tileEntity != null)
                            world.getChunkFromBlockCoords(pos).addTileEntity(tileEntity);
                    }

                    // TODO: Overwrite existing blocks (or replace them only if not the same??? =>
                    // decide based on a benchmark)

                }
            }
        }
    }

    private static void processTileEntities(Box box, List<NBTTagCompound> NBTTileEntities, World world)
    {
        for (NBTTagCompound NBTTileEntity : NBTTileEntities) {
            TileEntity.create(world, NBTTileEntity); // XXX: Will probably fail if TE already exists
        }
    }

    private static void saveSnapshot(String filename, Box box, World world)
    {
        NBTTagCompound mainCompound = new NBTTagCompound();
        mainCompound.setIntArray("minVertex", new int[] { box.minX, box.minY, box.minZ });
        mainCompound.setIntArray("maxVertex", new int[] { box.maxX, box.maxY, box.maxZ });

        // Buffers
        NBTTagList NBTBlocks = new NBTTagList();
        NBTTagList NBTTileEntities = new NBTTagList();

        // Iterate blocks within bounding box
        for (int x = (int) box.minX; x <= (int) box.maxX; x++) {
            for (int y = (int) box.minY; y <= (int) box.maxY; y++) {
                for (int z = (int) box.minZ; z <= (int) box.maxZ; z++) {
                    BlockPos pos = new BlockPos(x, y, z);

                    // Save block state
                    IBlockState state = world.getBlockState(pos);
                    Block block = state.getBlock();
                    short payload = (short) ((Block.getIdFromBlock(block) << 8) + block.getMetaFromState(state));
                    NBTBlocks.appendTag(new NBTTagShort(payload));

                    // XXX: debug
                    System.out.format("(%d,%d,%d): %s (id=%d)  meta=%d  tile=%b data=%d%n", x, y, z,
                            block.getLocalizedName(), Block.getIdFromBlock(block), block.getMetaFromState(state),
                            block.hasTileEntity(null), payload);

                    // Save tile entity
                    if (block.hasTileEntity(null)) {
                        TileEntity tileEntity = world.getChunkFromBlockCoords(pos).getTileEntity(pos,
                                Chunk.EnumCreateEntityType.IMMEDIATE);
                        NBTTagCompound compound = new NBTTagCompound();
                        compound = tileEntity.writeToNBT(compound);
                        NBTTileEntities.appendTag(compound);
                        System.out.println(tileEntity);
                    }
                }
            }
        }

        // Merge tag lists into one single NBT compound
        mainCompound.setTag("blocks", NBTBlocks);
        mainCompound.setTag("tile_entities", NBTTileEntities);

        // Save NBT compound to file
        FileOutputStream fileOut = null;
        try {
            fileOut = new FileOutputStream(filename);
        } catch (FileNotFoundException e1) {
            e1.printStackTrace();
        }
        DataOutputStream dataoutputstream = new DataOutputStream(
                new BufferedOutputStream(new DeflaterOutputStream(fileOut)));
        try {
            CompressedStreamTools.write(mainCompound, dataoutputstream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            dataoutputstream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static NBTTagCompound loadSnapshot(String filename)
    {
        NBTTagCompound mainCompound = null;

        FileInputStream fileIn = null;
        try {
            fileIn = new FileInputStream(filename);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        DataInputStream datainputstream = new DataInputStream(new BufferedInputStream(new InflaterInputStream(fileIn)));
        try {
            mainCompound = CompressedStreamTools.read(datainputstream);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return mainCompound;
    }
}
