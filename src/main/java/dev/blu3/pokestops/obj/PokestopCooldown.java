package dev.blu3.pokestops.obj;

import java.util.UUID;

public class PokestopCooldown {
    public UUID pokestopUUID;
    public long cooldownTime;

    public PokestopCooldown(UUID pokestopUUID, long cooldownTime) {
        this.pokestopUUID = pokestopUUID;
        this.cooldownTime = cooldownTime;
    }
}
