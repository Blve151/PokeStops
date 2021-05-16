package dev.blu3.pokestops.obj;

public class PokestopReward {
    public String itemID;
    public String itemDisplay;
    public int itemQuantity;
    public int itemMeta;
    public int weight;
    public String[] commands;

    public PokestopReward(String itemID, String itemDisplay, int itemQuantity, int itemMeta, int weight, String[] commands) {
        this.itemID = itemID;
        this.itemDisplay = itemDisplay;
        this.itemQuantity = itemQuantity;
        this.itemMeta = itemMeta;
        this.weight = weight;
        this.commands = commands;
    }
}
