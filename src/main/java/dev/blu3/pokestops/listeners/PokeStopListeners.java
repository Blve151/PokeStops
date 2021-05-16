package dev.blu3.pokestops.listeners;

import ca.landonjw.gooeylibs2.implementation.tasks.Task;
import com.pixelmonmod.pixelmon.entities.EntityPokestop;
import com.pixelmonmod.pixelmon.items.ItemTM;
import dev.blu3.pokestops.PokeStops;
import dev.blu3.pokestops.obj.PokestopCooldown;
import dev.blu3.pokestops.obj.PokestopType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.server.SPacketEntityMetadata;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.EnumHand;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.server.FMLServerHandler;

import java.util.*;
import java.util.concurrent.TimeUnit;

import static dev.blu3.pokestops.utils.Utils.openDropInventory;
import static dev.blu3.pokestops.utils.Utils.regex;

public class PokeStopListeners {
    public static HashMap<UUID, Boolean> removePokestopMap = new HashMap<>();
    HashMap<UUID, Set<PokestopCooldown>> pokestopCooldownMap = new HashMap<>();

    @SubscribeEvent
    public void onPokeStopInteract(PlayerInteractEvent.EntityInteractSpecific event) {
        if (!(event.getTarget() instanceof EntityPokestop)) {
            return;
        }


        EntityPokestop pokestop = (EntityPokestop) event.getTarget();
        EntityPlayerMP player = (EntityPlayerMP) event.getEntityPlayer();

        if(removePokestopMap.get(player.getUniqueID()) != null){
            event.setCanceled(true);
            pokestop.setDead();
            removePokestopMap.remove(player.getUniqueID());
            player.sendMessage(new TextComponentString(regex("&cDeleted PokeStop successfully.")));
            return;
        }

        if (!pokestop.getEntityData().hasKey("pokestopType")) return;
        if(event.getHand() == EnumHand.OFF_HAND) return;
        Optional<PokestopType> optPokestopType = PokeStops.dataManager.lookup(pokestop);
        if (!optPokestopType.isPresent()) return;

        PokestopType pokestopType = optPokestopType.get();
        Set<PokestopCooldown> pokestopCooldowns = pokestopCooldownMap.get(player.getUniqueID()) == null ? new HashSet<>() : pokestopCooldownMap.get(player.getUniqueID());
        Optional<PokestopCooldown> optCooldown = pokestopCooldowns.stream().filter(pokestopCooldown -> pokestop.getUniqueID().equals(pokestopCooldown.pokestopUUID)).findFirst();

        if (optCooldown.isPresent()) {
            long secondsLeft = ((optCooldown.get().cooldownTime) / 1000) + pokestopType.cooldownSeconds - (System.currentTimeMillis() / 1000);
            String stringLeft;
            if (TimeUnit.SECONDS.toMinutes(secondsLeft) == 0) {
                stringLeft = String.format("%d sec(s)", TimeUnit.SECONDS.toSeconds(secondsLeft));
            } else {
                stringLeft = String.format("%d min(s)", TimeUnit.SECONDS.toMinutes(secondsLeft));
            }
            player.sendMessage(new TextComponentString(regex("&cYou can't use this PokeStop for " + stringLeft + ".")));
            event.setCanceled(true);
        } else {
            player.sendMessage(new TextComponentString(regex("&bYou claimed the PokeStop.")));
            pokestopCooldowns.add(new PokestopCooldown(pokestop.getUniqueID(), System.currentTimeMillis()));
            pokestopCooldownMap.put(player.getUniqueID(), pokestopCooldowns);

            openDropInventory(player, pokestopType);
            EntityPokestop fakePokestop = new EntityPokestop(pokestop.world, pokestop.posX, pokestop.posY, pokestop.posZ);
            fakePokestop.setSize(pokestop.getSize());
            fakePokestop.setAlwaysAnimate(true);
            fakePokestop.animate();
            RGB c1 = pokestopType.rgbCooldownColor;
            RGB c2 = pokestopType.rgbBaseColor;

            setPokestopColorForPlayer(player, fakePokestop, pokestop, c1.red, c1.green, c1.blue);

            Task.builder()
                    .execute(task -> {
                        Set<PokestopCooldown> pokestopCooldowns1 = pokestopCooldownMap.get(player.getUniqueID());
                        Optional<PokestopCooldown> optCooldown1 = pokestopCooldowns1.stream().filter(pokestopCooldown -> pokestop.getUniqueID().equals(pokestopCooldown.pokestopUUID)).findFirst();
                        if (optCooldown1.isPresent()) {
                            pokestopCooldowns1.remove(optCooldown1.get());
                            pokestopCooldownMap.put(player.getUniqueID(), pokestopCooldowns1);
                        }
                        setPokestopColorForPlayer(player, fakePokestop, pokestop, c2.red, c2.green, c2.blue);
                    })
                    .delay(20L * pokestopType.cooldownSeconds)
                    .build();
        }
    }

    @SubscribeEvent
    public void onPokeStopRewardFound(TickEvent.PlayerTickEvent event) {
        EntityPlayer player = event.player;
        for (ItemStack stack : player.inventory.mainInventory) {
            if (stack != null && stack.getTagCompound() != null) {
                NBTTagCompound tagCompound = stack.getTagCompound();
                String[] commands;
                if (tagCompound.hasKey("pokestopRemove")) {
                    commands = tagCompound.getString("pokestopRewardCommands").split(";");
                    stack.setCount(0);
                    MinecraftServer server = FMLServerHandler.instance().getServer();
                    for (String string : commands) {
                        server.getCommandManager().executeCommand(server, string.replace("<player>", player.getName()));
                    }
                }
                if (tagCompound.hasKey("pokestopReward")) {
                    stack.setTagCompound(new NBTTagCompound());
                }
            }
        }
    }

    @SubscribeEvent
    public void onTMFound(TickEvent.PlayerTickEvent event) {
        EntityPlayer player = event.player;
        for (ItemStack stack : player.inventory.mainInventory) {
            if (stack != null && stack.getItem() instanceof ItemTM) {
                stack.setStackDisplayName(stack.getDisplayName().replaceFirst("\\w+\\s", ""));
            }
        }
    }

    private void setPokestopColorForPlayer(EntityPlayerMP player, EntityPokestop fakePokestop, EntityPokestop pokestop, int r, int g, int b) {
        fakePokestop.setColor(r, g, b);
        SPacketEntityMetadata entityAttributesPacket1 = new SPacketEntityMetadata(pokestop.getEntityId(), fakePokestop.getDataManager(), true);
        player.connection.sendPacket(entityAttributesPacket1);
    }
}
