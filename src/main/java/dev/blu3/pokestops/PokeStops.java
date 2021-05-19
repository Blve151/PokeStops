package dev.blu3.pokestops;

import dev.blu3.pokestops.commands.PokestopsCMD;
import dev.blu3.pokestops.config.PokeStopDataManager;
import dev.blu3.pokestops.listeners.PokeStopListeners;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.fml.common.Mod;

import java.io.IOException;

@Mod(
        modid = PokeStops.MOD_ID,
        name = PokeStops.MOD_NAME,
        version = PokeStops.VERSION,
        acceptableRemoteVersions = "*"
)
public class PokeStops {

    public static final String MOD_ID = "pokestops";
    public static final String MOD_NAME = "PokeStops";
    public static final String VERSION = "1.0.1";
    public static PokeStopDataManager dataManager = new PokeStopDataManager();
    @Mod.Instance(MOD_ID)
    public static PokeStops INSTANCE;

    @Mod.EventHandler
    public void onPreInit(FMLPreInitializationEvent event) throws IOException{
        MinecraftForge.EVENT_BUS.register(new PokeStopListeners());
        dataManager.load();
    }

    @Mod.EventHandler
    public void onServerStarting(FMLServerStartingEvent event) {
        event.registerServerCommand(new PokestopsCMD());
    }
}
