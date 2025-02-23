package de.catstorm.optout.optout;

public class PlayerScheduledForRemoval {
    public String playerName;
    public int scheduledTick;

    public PlayerScheduledForRemoval(String playerName, int scheduledTick) {
        this.playerName = playerName;
        this.scheduledTick = scheduledTick;
    }
}
