package claudy;

import java.io.File;
import net.minecraftforge.common.config.Configuration;

public class ModConfig
{
    private Configuration config;

    public static String SNAPSHOT_DRECTORY = "claudy_snapshots";
    public static String SNAPSHOT_EXTENSION = ".x";
    public static boolean SAVE_TILE_ENTITIES = true;
    public static int MAX_BOX_VOLUME = 10000000;

    public ModConfig(File configFile)
    {
        this.config = new Configuration(configFile);
        this.config.load();
        this.load();
        this.config.save();
    }

    private void load()
    {
        SNAPSHOT_DRECTORY = config.getString("SnapshotDirectory", Configuration.CATEGORY_GENERAL, SNAPSHOT_DRECTORY, "");
        SNAPSHOT_EXTENSION = config.getString("SnapshotExtension", Configuration.CATEGORY_GENERAL, SNAPSHOT_EXTENSION, "");
        SAVE_TILE_ENTITIES = config.getBoolean("saveTileEntities", Configuration.CATEGORY_GENERAL, SAVE_TILE_ENTITIES, "");
        MAX_BOX_VOLUME = config.getInt("MaxBoxVolume", Configuration.CATEGORY_GENERAL, MAX_BOX_VOLUME, 0, Integer.MAX_VALUE, "");
    }
}
