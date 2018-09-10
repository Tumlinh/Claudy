package claudy;

import java.io.File;
import claudy.ModConfig;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;

@Mod(modid = Claudy.MODID, name = Claudy.NAME, version = Claudy.VERSION, acceptedMinecraftVersions = "[1.12,)", acceptableRemoteVersions = "*")
public class Claudy
{
    public static final String MODID = "claudy";
    public static final String NAME = "Claudy";
    public static final String VERSION = "@MOD_VERSION@";

    @Instance(MODID)
    public static Claudy instance;

    @EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        // Load configuration
        File configFile = new File(event.getModConfigurationDirectory(), "claudy.cfg");
        new ModConfig(configFile);
    }

    @EventHandler
    public void serverStarting(FMLServerStartingEvent event)
    {
        event.registerServerCommand(new CommandSnapshot());
    }
}
