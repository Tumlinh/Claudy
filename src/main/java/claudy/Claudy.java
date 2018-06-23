package claudy;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;

@Mod(modid = Claudy.MODID, name = Claudy.NAME, version = Claudy.VERSION, acceptedMinecraftVersions = "[1.12,)", acceptableRemoteVersions = "*")
public class Claudy
{
    public static final String MODID = "claudy";
    public static final String NAME = "Claudy";
    public static final String VERSION = "@VMOD_VERSION@";
    
    @Instance(MODID)
    public static Claudy instance;
    
    // TODO: Configuration
    
    @EventHandler
    public void serverStarting(FMLServerStartingEvent event)
    {
        event.registerServerCommand(new CommandSnapshot());
    }
}
