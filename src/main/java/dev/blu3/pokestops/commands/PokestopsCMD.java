package dev.blu3.pokestops.commands;

import com.pixelmonmod.pixelmon.entities.EntityPokestop;
import dev.blu3.pokestops.PokeStops;
import dev.blu3.pokestops.listeners.RGB;
import dev.blu3.pokestops.obj.PokestopType;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static dev.blu3.pokestops.listeners.PokeStopListeners.removePokestopMap;
import static dev.blu3.pokestops.utils.Utils.regex;

public class PokestopsCMD extends CommandBase {

    @Override
    public String getName() {
        return "pokestops";
    }

    @Override
    public String getUsage(ICommandSender sender) {
        return "/pokestops <create> <name>/<delete>/<reload>";
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {
        List<String> tabCompletions = new ArrayList<>();
        if (sender.canUseCommand(0, "pokestops.command.pokestops")) {
            if (args.length == 1) {
                tabCompletions.add("create");
                tabCompletions.add("delete");
                tabCompletions.add("reload");
            }
            if (args.length > 1) {
                switch (args[0]) {
                    case "create":
                        tabCompletions.addAll(PokeStops.dataManager.getPokeStops());
                        break;
                }
            }

        }
        return tabCompletions;
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {

        if (args.length < 1) {
            sendUsage(sender);
            return;
        }

        EntityPlayerMP player;
        switch (args[0]) {
            case "create":
                if (!(sender instanceof EntityPlayerMP)) {
                    sender.sendMessage(new TextComponentString(regex("&cOnly players can use this command!")));
                    return;
                }
                player = (EntityPlayerMP) sender;
                if (args.length == 2) {
                    World world = player.world;
                    String arg = args[1];
                    Optional<PokestopType> opt = PokeStops.dataManager.lookup(arg);
                    if (!opt.isPresent()) {
                        player.sendMessage(new TextComponentString(regex("&cInvalid PokeStop, check the files or tab complete for more.")));
                        return;
                    }
                    PokestopType pokestopType = opt.get();
                    BlockPos pos = player.getPosition();
                    EntityPokestop pokestop = new EntityPokestop(world, pos.getX(), pos.getY(), pos.getZ());
                    RGB c = pokestopType.rgbBaseColor;
                    pokestop.setSize((float) pokestopType.size);
                    pokestop.setColor(c.red, c.green, c.blue);
                    pokestop.getEntityData().setString("pokestopType", arg);

                    if(world.spawnEntity(pokestop)){
                        sender.sendMessage(new TextComponentString(regex("&aSuccessfully spawned PokeStop of type: &e" + arg)));
                        pokestop.setAlwaysAnimate(true);
                        pokestop.animate();
                    }else{
                        sender.sendMessage(new TextComponentString(regex("&cError spawning the PokeStop.")));
                    }
                } else {
                    sendUsage(player);
                }
                break;
            case "delete":
                if (!(sender instanceof EntityPlayerMP)) {
                    sender.sendMessage(new TextComponentString(regex("&cOnly players can use this command!")));
                    return;
                }
                player = (EntityPlayerMP) sender;
                removePokestopMap.put(player.getUniqueID(), true);
                player.sendMessage(new TextComponentString(regex("&eRight-click a PokeStop to delete it!")));
                break;
            case "reload":
                try{
                    PokeStops.dataManager.load();
                    sender.sendMessage(new TextComponentString(regex("&bReloaded PokeStops config successfully.")));
                }catch (Exception ex){
                    sender.sendMessage(new TextComponentString(regex("&eError while loading config, check the console for errors.")));
                    ex.printStackTrace();
                }
                break;
            default:
                sendUsage(sender);
                break;

        }
    }

    private void sendUsage(ICommandSender sender) {
        sender.sendMessage(new TextComponentString(regex("&cInvalid usage.")));
        sender.sendMessage(new TextComponentString(getUsage(sender)));
    }
}
