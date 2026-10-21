package com.opsec;

import net.fabricmc.api.ClientModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OpsecMod implements ClientModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("opsec");

    @Override
    public void onInitializeClient() {
        OpsecConfig c = OpsecConfig.get();
        LOGGER.info("[opsec] loaded | brand={} payloadFilter={} translations={} keybinds={}",
                c.spoofBrand ? c.brand : "off", c.filterPayloads, c.hideModTranslations, c.hideModKeybinds);
    }
}
