package dev.blu3.pokestops.utils;

import com.pixelmonmod.pixelmon.Pixelmon;
import com.pixelmonmod.pixelmon.comm.packetHandlers.itemDrops.ItemDropMode;
import com.pixelmonmod.pixelmon.comm.packetHandlers.itemDrops.ItemDropPacket;
import com.pixelmonmod.pixelmon.entities.pixelmon.drops.DropItemQuery;
import com.pixelmonmod.pixelmon.entities.pixelmon.drops.DropItemQueryList;
import com.pixelmonmod.pixelmon.entities.pixelmon.drops.DroppedItem;
import dev.blu3.pokestops.obj.PokestopReward;
import dev.blu3.pokestops.obj.PokestopType;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextComponentTranslation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {

    private static final String regex = "&(?=[0-9a-ff-or])";
    private static final Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);

    public static String regex(String line) {
        Matcher matcher = pattern.matcher(line);
        if (matcher.find()) {
            line = line.replaceAll(regex, "§");
        }
        return line;
    }

    public static void openDropInventory(EntityPlayerMP player, PokestopType pokestopType) {
        ArrayList<DroppedItem> droppedItems = new ArrayList<>();
        int id = -1;

        List<PokestopReward> rewardList = pokestopType.rewardList;
        if (pokestopType.rewardQuantity != -1) {
            List<PokestopReward> newList = new ArrayList<>();

            for (PokestopReward pokestopReward : rewardList) {
                for (int i = 0; i < pokestopReward.weight; i++) {
                    newList.add(pokestopReward);
                }
            }

            rewardList = new ArrayList<>();
            for (int i = 0; i < pokestopType.rewardQuantity; i++) {
                rewardList.add(newList.get(new Random().nextInt(newList.size())));
            }
        }

        for (PokestopReward item : rewardList) {
            id++;
            ItemStack stack;
            stack = new ItemStack(Item.getByNameOrId(item.itemID));
            NBTTagCompound nbtTagCompound = stack.getTagCompound() == null ? new NBTTagCompound() : stack.getTagCompound();
            nbtTagCompound.setBoolean("pokestopReward", true);
            String commands = "";
            for (String command : item.commands) {
                commands += command + ";";
            }
            if (commands.length() > 0) {
                nbtTagCompound.setBoolean("pokestopRemove", true);
                commands = commands.substring(0, commands.length() - 1);
            }
            nbtTagCompound.setString("pokestopRewardCommands", commands);
            stack.setTagCompound(nbtTagCompound);
            stack.setCount(item.itemQuantity);
            stack.setItemDamage(item.itemMeta);
            stack.setStackDisplayName(regex(item.itemDisplay));
            droppedItems.add(new DroppedItem(stack, id));
        }
        DropItemQuery query = new DropItemQuery(player.getPositionVector(), player.getUniqueID(), droppedItems);
        DropItemQueryList.queryList.add(query);
        ItemDropPacket packet = new ItemDropPacket(ItemDropMode.Other, new TextComponentTranslation(regex("&bPokeStop &7- &d" + pokestopType.pokestopName)), droppedItems);
        Pixelmon.network.sendTo(packet, player);
    }

}
