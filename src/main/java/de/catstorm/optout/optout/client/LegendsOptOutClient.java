package de.catstorm.optout.optout.client;

import net.fabricmc.api.ClientModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LegendsOptOutClient implements ClientModInitializer {
    public static final String MOD_ID = "optout";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitializeClient() {
        LOGGER.info("LegendsOptOutClient initialised!");
    }
}