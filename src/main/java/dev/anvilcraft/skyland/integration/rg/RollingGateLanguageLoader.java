package dev.anvilcraft.skyland.integration.rg;

import com.google.gson.reflect.TypeToken;
import dev.anvilcraft.rg.api.server.TranslationUtil;
import dev.anvilcraft.skyland.Skyland;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * RollingGate 1.1.0 always stores files loaded through loadLanguage under zh_cn.
 * Parse the resources here so the selected language is registered under its actual locale.
 */
final class RollingGateLanguageLoader {
    private static final String[] LOCALES = {"en_us", "zh_cn"};

    private RollingGateLanguageLoader() {
    }

    static void loadAll() {
        for (String locale : LOCALES) {
            load(locale);
        }
    }

    private static void load(String locale) {
        String path = "/assets/%s/lang/%s.json".formatted(Skyland.MOD_ID, locale);
        try (InputStream stream = RollingGateIntegration.class.getResourceAsStream(path)) {
            if (stream == null) {
                Skyland.LOGGER.error("Can't find RollingGate language resource {}", path);
                return;
            }
            try (Reader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
                Map<String, String> translations = TranslationUtil.GSON.fromJson(
                    reader,
                    new TypeToken<Map<String, String>>() {
                    }.getType()
                );
                if (translations == null) {
                    Skyland.LOGGER.error("RollingGate language resource {} is empty", path);
                    return;
                }
                TranslationUtil.addLanguage(locale, translations);
            }
        } catch (IOException exception) {
            Skyland.LOGGER.error("Failed to load RollingGate language resource {}", path, exception);
        }
    }
}
