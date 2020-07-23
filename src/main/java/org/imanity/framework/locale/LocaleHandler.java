package org.imanity.framework.locale;

import lombok.Getter;
import org.bukkit.configuration.file.YamlConfiguration;
import org.imanity.framework.Imanity;
import org.imanity.framework.locale.player.LocaleData;
import org.imanity.framework.locale.type.LocaleTypeData;
import org.imanity.framework.player.data.PlayerDataBuilder;
import org.imanity.framework.player.data.type.DataType;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class LocaleHandler {

    private final Map<String, Locale> locales = new HashMap<>();
    @Getter
    private Locale defaultLocale;

    public void init() {
        DataType.register(Locale.class, LocaleTypeData.class);

        this.defaultLocale = this.registerLocale(Imanity.CORE_CONFIG.DEFAULT_LOCALE);

        new PlayerDataBuilder()
                .loadOnJoin(true)
                .saveOnQuit(true)
                .name("LocaleData")
                .playerDataClass(LocaleData.class)
                .build();
    }

    public Locale registerLocale(String name) {
        Locale locale;

        if (this.locales.containsKey(name)) {
            locale = this.locales.get(name);
        } else {
            locale = new Locale(name);
            this.locales.put(name, locale);
        }

        return locale;
    }

    public Locale registerFromYml(File file) {

        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        String name = config.getString("locale");

        Locale locale = this.registerLocale(name);

        for (String key : config.getKeys(true)) {
            locale.registerEntry(key, config.getString(key));
        }

        return locale;

    }

    public void unregisterLocale(String name) {
        this.locales.remove(name);
    }

    public Locale getLocale(String name) {
        if (this.locales.containsKey(name)) {
            return this.locales.get(name);
        }

        return null;
    }

}
