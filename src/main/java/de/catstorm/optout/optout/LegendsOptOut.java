package de.catstorm.optout.optout;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class LegendsOptOut implements ModInitializer {
    public static final String MOD_ID = "optout";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static Set<String> optedInPlayerNames = new HashSet<>();
    public static Set<PlayerScheduledForRemoval> playersScheduledForRemoval = new HashSet<>();

    @Override
    public void onInitialize() {
        LOGGER.info("LegendsOptOut initialised!");

        ServerTickEvents.START_SERVER_TICK.register((server -> {
            for (var player : playersScheduledForRemoval) {
                if (player.scheduledTick == server.getTicks()) {
                    playersScheduledForRemoval.remove(player);
                    optedInPlayerNames.remove(player.playerName);
                    try {
                        Objects.requireNonNull(server.getPlayerManager().getPlayer(player.playerName)).sendMessage(Text.of("You are now opted out!"));
                    }
                    catch (Exception e) {
                        LOGGER.info("If you're reading this then cat seriously fucked up.");
                    }
                }
            }
        }));

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            if (optedInPlayerNames.contains(handler.getPlayer().getNameForScoreboard()))
                handler.getPlayer().sendMessage(Text.of("You are opted in!"));
            else
                handler.getPlayer().sendMessage(Text.of("You are opted out!"));
        });

        CommandRegistrationCallback.EVENT.register(((dispatcher, registryAccess, environment) -> dispatcher.register(CommandManager.literal("opt")
            .then(CommandManager.literal("in")
                .executes(context -> {
                    try {
                        ServerCommandSource source = context.getSource();
                        for (var player : playersScheduledForRemoval) {
                            if (Objects.requireNonNull(source.getPlayer()).getNameForScoreboard().equals(player.playerName)) {
                                playersScheduledForRemoval.remove(player);
                                source.getPlayer().sendMessage(Text.of("Your opt-out has been aborted!"));
                            }
                        }
                        optedInPlayerNames.add(Objects.requireNonNull(source.getPlayer()).getNameForScoreboard());
                        source.getPlayer().sendMessage(Text.of("You are now opted in!"));
                        return 1;
                    }
                    catch (Exception e) {
                        return 0;
                    }
                }))
            .then(CommandManager.literal("out")
                .executes(context -> {
                    if (context.getSource().getPlayer() == null) return 0;
                    for (var player : playersScheduledForRemoval) {
                        if (player.playerName.equals(context.getSource().getPlayer().getNameForScoreboard())) return 0;
                    }
                    try {
                        ServerCommandSource source = context.getSource();
                        if (optedInPlayerNames.contains(Objects.requireNonNull(source.getPlayer()).getNameForScoreboard())) {
                            source.getPlayer().sendMessage(Text.of("Opting out in 20 seconds!"));
                            playersScheduledForRemoval.add(new PlayerScheduledForRemoval(source.getPlayer().getNameForScoreboard(),
                                Objects.requireNonNull(source.getPlayer().getServer()).getTicks() + 400));
                        } else {
                            source.getPlayer().sendMessage(Text.of("You are already opted out!"));
                        }
                        return 1;
                    } catch (Exception e) {
                        return 0;
                    }
                }
            )
        ))));
    }
}