package net.isaiah.deathstatues.config;

import com.terraformersmc.modmenu.config.option.OptionConvertable;
import net.isaiah.deathstatues.option.CustomBooleanConfigOption;
import net.minecraft.client.option.SimpleOption;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;

public class DeathStatueConfig {
    public static final CustomBooleanConfigOption BASE_PLACES_ENTITY = new CustomBooleanConfigOption("base_places_entity", false);
    public static final CustomBooleanConfigOption BASE_PLACES_BLOCK = new CustomBooleanConfigOption("base_places_block", false);

    public static SimpleOption<?>[] asOptions() {
        ArrayList<SimpleOption<?>> options = new ArrayList<>();
        for (Field field : DeathStatueConfig.class.getDeclaredFields()) {
            if (Modifier.isStatic(field.getModifiers())
                    && Modifier.isFinal(field.getModifiers())
                    && OptionConvertable.class.isAssignableFrom(field.getType())
                    && !field.getName().equals("HIDE_CONFIG_BUTTONS")
                    && !field.getName().equals("MODIFY_TITLE_SCREEN")
                    && !field.getName().equals("MODIFY_GAME_MENU")
                    && !field.getName().equals("CONFIG_MODE")
                    && !field.getName().equals("DISABLE_DRAG_AND_DROP")
            ) {
                try {
                    options.add(((OptionConvertable) field.get(null)).asOption());
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
        return options.stream().toArray(SimpleOption[]::new);
    }
}
