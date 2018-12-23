package claudy;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.Iterator;

import claudy.utils.Box;
import claudy.utils.NBTUtil;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagShort;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;

public class Snapshot
{
    private String label;
    private Box box;
    private World world;
    private long creationTime;

    private static class BlockPayload
    {
        private int id;
        private int metadata;

        public BlockPayload(IBlockState state)
        {
            Block block = state.getBlock();
            id = Block.getIdFromBlock(block);
            metadata = block.getMetaFromState(state);
        }

        public BlockPayload(short payload)
        {
            id = (payload & 0xFFFF) >> 8; // Shift payload as unsigned short
            metadata = payload & 0xFF;
        }

        public short compute()
        {
            return (short) ((id << 8) + metadata);
        }
    }

    public Snapshot(String label, Box box, World world)
    {
        this.label = label;
        this.box = box;
        this.world = world;
    }

    public void save() throws IOException
    {
        Path dirPath = Paths.get(Claudy.instance.snapshotPath.toURI());
        dirPath.toFile().mkdirs();
        Path fullPath = dirPath.resolve(label + ModConfig.SNAPSHOT_EXTENSION);
        saveBox(fullPath.toString(), box, world);
    }

    public void restore() throws IOException
    {
        // Load NBT from file
        Path dirPath = Paths.get(Claudy.instance.snapshotPath.toURI());
        Path fullPath = dirPath.resolve(label + ModConfig.SNAPSHOT_EXTENSION);
        NBTTagCompound mainCompound = NBTUtil.loadNBT(fullPath.toString());

        // Extract headers
        int dimension = mainCompound.getByte("dimension");
        int[] minVertex = mainCompound.getIntArray("minVertex");
        int[] maxVertex = mainCompound.getIntArray("maxVertex");
        long creationTime = mainCompound.getLong("time");

        Box box = new Box(minVertex[0], minVertex[1], minVertex[2], maxVertex[0], maxVertex[1], maxVertex[2]);
        this.box = box;
        this.creationTime = creationTime;

        // TODO: Configuration: option for choosing between blocks, tile entities or
        // both

        // Extract and process payload
        NBTTagList NBTBlocks = mainCompound.getTagList("blocks", new NBTTagShort().getId());
        NBTTagList NBTTileEntities = mainCompound.getTagList("tile_entities", new NBTTagCompound().getId());
        World world = DimensionManager.getWorld(dimension);
        restoreBox(box, NBTBlocks, NBTTileEntities, world, true);
    }

    private static void restoreBox(Box box, NBTTagList NBTBlocks, NBTTagList NBTTileEntities, World world,
            boolean notifyNeighbours)
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
                    BlockPayload blockPayload = new BlockPayload(payload);
                    Block block = Block.getBlockById(blockPayload.id);
                    IBlockState blockState = block.getStateFromMeta(blockPayload.metadata);

                    // XXX: debug
                    /*
                     * if (Block.getIdFromBlock(block) == 84) { System.out.printf("gotcha!%n"); }
                     */

                    // Compare current block with the one from snapshot
                    IBlockState currentBlockState = world.getBlockState(pos);
                    BlockPayload currentBlockPayload = new BlockPayload(currentBlockState);
                    // TODO: Benchmark code
                    // if (currentBlockPayload.compute() != blockPayload.compute()) {

                    // Replace block
                    world.setBlockState(pos, blockState, notifyNeighbours ? 3 : 2);

                    // TODO: notify replaced block?
                    // world.notifyBlockUpdate(pos, currentBlockState, blockState, notifyNeighbours
                    // ? 3 : 2);
                    // }

                    if (block.hasTileEntity(null)) {
                        NBTTagCompound NBTTileEntity = (NBTTagCompound) tileEntitiesIterator.next();
                        TileEntity tileEntity = TileEntity.create(world, NBTTileEntity);
                        // XXX: Wrong order of tile entities here?
                        /*
                         * System.out.println(blockState); System.out.println(tileEntity);
                         * System.out.println();
                         */
                        if (tileEntity != null)
                            world.addTileEntity(tileEntity);
                    }
                }
            }
        }
    }

    private static void saveBox(String filename, Box box, World world) throws IOException
    {
        NBTTagCompound mainCompound = new NBTTagCompound();
        mainCompound.setByte("dimension", (byte) world.provider.getDimension());
        mainCompound.setIntArray("minVertex", new int[] { box.minX, box.minY, box.minZ });
        mainCompound.setIntArray("maxVertex", new int[] { box.maxX, box.maxY, box.maxZ });
        mainCompound.setLong("time", (new Date()).getTime());

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
                    short payload = (new BlockPayload(state)).compute();
                    NBTBlocks.appendTag(new NBTTagShort(payload));

                    // XXX: debug
                    /*
                     * if (Block.getIdFromBlock(block) == 84) { System.out.printf("gotcha!%n"); }
                     */
                    /*
                     * System.out.format("(%d,%d,%d): %s (id=%d)  meta=%d  tile=%b data=%d%n", x, y,
                     * z, block.getLocalizedName(), Block.getIdFromBlock(block),
                     * block.getMetaFromState(state), block.hasTileEntity(null), payload);
                     */

                    // Save tile entity
                    if (block.hasTileEntity(null) && ModConfig.SAVE_TILE_ENTITIES) {
                        TileEntity tileEntity = world.getTileEntity(pos);
                        NBTTagCompound compound = new NBTTagCompound();
                        compound = tileEntity.writeToNBT(compound);
                        NBTTileEntities.appendTag(compound);
                    }
                }
            }
        }

        // Merge tag lists into one single NBT compound
        mainCompound.setTag("blocks", NBTBlocks);
        mainCompound.setTag("tile_entities", NBTTileEntities);

        NBTUtil.saveNBT(filename, mainCompound);
    }

    public String getLabel()
    {
        return label;
    }

    public Box getBox()
    {
        return box;
    }

    public World getWorld()
    {
        return world;
    }

    public long getCreationTime()
    {
        return creationTime;
    }
}
