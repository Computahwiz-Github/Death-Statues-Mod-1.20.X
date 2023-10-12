package net.isaiah.deathstatues;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import net.isaiah.deathstatues.screen.DeathStatuesConfigScreen;

public class DeathStatuesModMenu implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return DeathStatuesConfigScreen::new;
    }
}
