package dev.blu3.pokestops.config;

import com.google.common.collect.Lists;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.pixelmonmod.pixelmon.entities.EntityPokestop;
import dev.blu3.pokestops.listeners.RGB;
import dev.blu3.pokestops.obj.PokestopReward;
import dev.blu3.pokestops.obj.PokestopType;

import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

public class PokeStopDataManager {

    private final Gson gson = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();
    private HashMap<String, PokestopType> pokestops = new HashMap<>();
    private final String NBT_KEY = "pokestopType";
    public final File presetDirectory = new File("./config/PokeStops/stops");

    public void load() throws IOException {
        if (!presetDirectory.exists()) {
            presetDirectory.mkdirs();
            File exampleFile = new File(presetDirectory, "common.json");
            exampleFile.createNewFile();

            BufferedWriter writer = new BufferedWriter(new FileWriter(exampleFile));
            PokestopType pokestopType = new PokestopType("Common", 5.0, 300, new RGB(30, 144, 255), new RGB(255,0,255), 1,
                    Lists.newArrayList(
                            new PokestopReward("minecraft:paper", "&d$100", 1, 0, 1, new String[]{"eco add <player> 100"}),
                            new PokestopReward("pixelmon:marsh_badge", "&dx1 Tokens", 1,0, 1, new String[]{"tokensadd <player> 1"}),
                            new PokestopReward("pixelmon:poke_ball", "&dx3 Poke Balls", 3, 0, 1, new String[]{})));
            writer.write(gson.toJson(pokestopType));
            writer.close();
        }

        pokestops = new HashMap<>();
        for (File file : presetDirectory.listFiles()) {
            BufferedReader br = new BufferedReader(new FileReader(file));
            PokestopType pokestopType = gson.fromJson(br, TypeToken.of(PokestopType.class).getType());
            if (!pokestops.containsKey(pokestopType.pokestopName)) {
                pokestops.put(pokestopType.pokestopName, pokestopType);
            }
            br.close();
        }
    }

    public Optional<PokestopType> lookup(EntityPokestop pokestop) {
        String key = pokestop.getEntityData().getString(NBT_KEY);
        PokestopType preset = pokestops.get(key);
        if (preset == null) {
            pokestop.getEntityData().removeTag(NBT_KEY);
        }
        return Optional.ofNullable(preset);
    }

    public Optional<PokestopType> lookup(String pokestopName) {
        return Optional.ofNullable(pokestops.get(pokestopName));
    }

    public List<String> getPokeStops (){
        return new ArrayList<>(pokestops.keySet());
    }
}
