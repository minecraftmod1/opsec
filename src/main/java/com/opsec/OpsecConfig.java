package com.opsec;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.util.Identifier;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class OpsecConfig {
    // 1. client brand
    public boolean spoofBrand = true;
    public String brand = "vanilla";

    // 2. block outgoing custom payloads (fabric channel registration etc.)
    public boolean filterPayloads = true;
    // add namespaces/channels here for mods that NEED a server counterpart, e.g. "journeymap"
    public List<String> allowedNamespaces = new ArrayList<>();
    public List<String> allowedChannels = new ArrayList<>();

    // 3. translation / keybind probing (signs, anvils, books)
    public boolean hideModTranslations = true;
    public boolean hideModKeybinds = true;
    // extra words that should count as "mod" in translation/keybind keys
    public List<String> extraHiddenKeyWords = new ArrayList<>();

    public boolean logBlocked = true;

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Set<String> GENERIC = Set.of(
            "minecraft", "java", "key", "keys", "block", "item", "entity", "gui", "options",
            "menu", "chat", "title", "effect", "biome", "category", "categories", "config", "misc");

    private static OpsecConfig instance;
    private transient Set<String> hidden;
    private transient final Set<String> logged = new HashSet<>();

    public static synchronized OpsecConfig get() {
        if (instance == null) instance = load();
        return instance;
    }

    private static OpsecConfig load() {
        Path path = FabricLoader.getInstance().getConfigDir().resolve("opsec.json");
        OpsecConfig cfg = new OpsecConfig();
        try {
            if (Files.exists(path)) {
                OpsecConfig read = GSON.fromJson(Files.readString(path), OpsecConfig.class);
                if (read != null) cfg = read;
            }
            Files.writeString(path, GSON.toJson(cfg));
        } catch (Exception e) {
            OpsecMod.LOGGER.warn("[opsec] config problem, using defaults", e);
        }
        return cfg;
    }

    public boolean shouldBlockPayload(Identifier id) {
        if (!filterPayloads) return false;
        String ns = id.getNamespace();
        if (ns.equals("minecraft") && id.getPath().equals("brand")) return false;
        if (allowedNamespaces.contains(ns)) return false;
        if (allowedChannels.contains(id.toString())) return false;
        log("blocked outgoing payload " + id);
        return true;
    }

    public boolean shouldHideKey(String key) {
        if (key == null) return false;
        Set<String> h = hidden();
        for (String part : key.toLowerCase(Locale.ROOT).split("[.:/]")) {
            if (h.contains(part)) {
                log("hid probe key " + key);
                return true;
            }
        }
        return false;
    }

    private synchronized Set<String> hidden() {
        if (hidden == null) {
            Set<String> s = new HashSet<>();
            for (ModContainer m : FabricLoader.getInstance().getAllMods()) {
                s.add(m.getMetadata().getId().toLowerCase(Locale.ROOT));
                m.getMetadata().getProvides().forEach(p -> s.add(p.toLowerCase(Locale.ROOT)));
            }
            for (String w : extraHiddenKeyWords) s.add(w.toLowerCase(Locale.ROOT));
            s.removeAll(GENERIC);
            hidden = s;
        }
        return hidden;
    }

    private synchronized void log(String msg) {
        if (logBlocked && logged.add(msg)) OpsecMod.LOGGER.info("[opsec] {}", msg);
    }
}
