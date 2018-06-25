package claudy;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import claudy.utils.Box;
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
        if (args.length == 1) {
            return getListOfStringsMatchingLastWord(args, "save", "restore");
        } else if (args.length == 2) {
            List<String> labels = new ArrayList<String>();
            // TODO: get dir from config file
            File[] files = new File("claudy_snapshots").listFiles(new FileFilter()
            {
                @Override
                public boolean accept(File file)
                {
                    return file.isFile() && file.getName().endsWith(".x");
                }
            });

            for (File file : files)
                labels.add(file.getName().substring(0, file.getName().length() - 2));

            return getListOfStringsMatchingLastWord(args, labels);
        }

        return null;
    }

    @Override
    public int getRequiredPermissionLevel()
    {
        return 2;
    }

    @Override
    public String getName()
    {
        return "claudy";
    }

    @Override
    public String getUsage(ICommandSender sender)
    {
        return "/claudy <save|restore> [...]";
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException
    {
        // TODO: check args length
        // TODO: autocompletion?

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

            // Check box volume for security purposes
            if (box.getVolume() > 10000000) {
                sendMessage(sender, "Volume is too big. Aborting", TextFormatting.RED);
            } else {
                long start = System.currentTimeMillis();

                try {
                    (new Snapshot(label, box, world)).save();
                } catch (IOException e) {
                    String msg = String.format("Failed saving snapshot '%s' (IOException)", label);
                    sendMessage(sender, msg, TextFormatting.RED);

                    return;
                }

                long duration = System.currentTimeMillis() - start;
                System.out.printf("duration=%d%n", duration);

                String msg = String.format("Saved snapshot '%s' (%d blocks)", label, box.getVolume());
                sendMessage(sender, msg, TextFormatting.BLUE);
            }
        } else if (args[0].equals("restore")) {
            long start = System.currentTimeMillis();

            Snapshot snapshot = new Snapshot(label, null, world);
            try {
                snapshot.restore();
            } catch (IOException e) {
                String msg = String.format("Failed restoring snapshot '%s' (IOException)", label);
                sendMessage(sender, msg, TextFormatting.RED);

                return;
            }

            long duration = System.currentTimeMillis() - start;
            System.out.printf("duration=%d%n", duration);

            SimpleDateFormat dt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss (Z)");
            Date date = new Date(snapshot.getCreationTime());
            String creationTime = dt.format(date);

            String msg = String.format("Restored snapshot '%s' (%d blocks)%nCreation time: %s", label,
                    snapshot.getBox().getVolume(), creationTime);
            sendMessage(sender, msg, TextFormatting.BLUE);
        }
    }

    private static void sendMessage(ICommandSender sender, String message, TextFormatting textFormatting)
    {
        ITextComponent msg = new TextComponentTranslation(message, new Object[0]);
        msg.getStyle().setColor(textFormatting);
        sender.sendMessage(msg);
    }
}
