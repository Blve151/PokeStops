package dev.blu3.pokestops.obj;

import dev.blu3.pokestops.listeners.RGB;

import java.awt.*;
import java.util.List;

public class PokestopType {
    public String pokestopName;
    public double size;
    public int cooldownSeconds;
    public RGB rgbBaseColor;
    public RGB rgbCooldownColor;
    public int rewardQuantity;
    public java.util.List<PokestopReward> rewardList;

    public PokestopType(String pokestopName, double size, int cooldownSeconds, RGB rgbBaseColor, RGB rgbCooldownColor, int rewardQuantity, List<PokestopReward> rewardList) {
        this.pokestopName = pokestopName;
        this.size = size;
        this.cooldownSeconds = cooldownSeconds;
        this.rgbBaseColor = rgbBaseColor;
        this.rgbCooldownColor = rgbCooldownColor;
        this.rewardQuantity = rewardQuantity;
        this.rewardList = rewardList;
    }
}
