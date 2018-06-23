package claudy;

import java.util.ArrayList;
import java.util.List;

import claudy.Snapshot.Box;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.WrongUsageException;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

public class CommandSnapshot extends CommandBase
{
    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, BlockPos pos)
    {
        List<String> suggestions = new ArrayList<String>();
        return getListOfStringsMatchingLastWord(args, suggestions);
    }

    @Override
    public int getRequiredPermissionLevel()
    {
        return 2;
    }

    @Override
    public String getName()
    {
        return "snapshot";
    }

    @Override
    public String getUsage(ICommandSender sender)
    {
        return "/snapshot <save|restore> [...]";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
    {
        if (args.length < 2)
            throw new WrongUsageException(this.getUsage(sender), new Object[0]);

        World world = sender.getEntityWorld();
        String label = args[1];

        if (args[0].equals("save")) {
            Vec3d vec3d = sender.getPositionVector();
            double d0 = vec3d.x;
            double d1 = vec3d.y;
            double d2 = vec3d.z;
            int x1 = (int) parseDouble(d0, args[2], true);
            int y1 = (int) parseDouble(d1, args[3], true);
            int z1 = (int) parseDouble(d2, args[4], true);
            int x2 = (int) parseDouble(d0, args[5], true);
            int y2 = (int) parseDouble(d1, args[6], true);
            int z2 = (int) parseDouble(d2, args[7], true);

            Box box = new Box(x1, y1, z1, x2, y2, z2);

            Snapshot.save(label, box, world);
        } else if (args[0].equals("restore")) {
            Snapshot.restore(label, world);
        }

        sendMessage(sender, "done");
    }

    private static void sendMessage(ICommandSender sender, String message)
    {
        ITextComponent msg = new TextComponentTranslation(message, new Object[0]);
        msg.getStyle().setColor(TextFormatting.BLUE);
        sender.sendMessage(msg);
    }
}
