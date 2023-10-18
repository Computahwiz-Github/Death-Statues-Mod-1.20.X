package net.isaiah.deathstatues.option;

import com.terraformersmc.modmenu.config.option.BooleanConfigOption;
import com.terraformersmc.modmenu.config.option.ConfigOptionStorage;
import com.terraformersmc.modmenu.util.TranslationUtil;
import net.minecraft.client.option.SimpleOption;
import net.minecraft.text.Text;

public class CustomBooleanConfigOption extends BooleanConfigOption {
    private final String translationKey;
    private final Text enabledText;
    private final Text disabledText;
    private final Text BASE_PLACES_ENTITY_TOOLTIP = Text.translatable("option.modmenu.base_places_entity.tooltip");
    private final Text BASE_PLACES_BLOCK_TOOLTIP = Text.translatable("option.modmenu.base_places_block.tooltip");
    public CustomBooleanConfigOption(String key, boolean defaultValue, String enabledKey, String disabledKey) {
        super(key, defaultValue, enabledKey, disabledKey);
        this.translationKey = TranslationUtil.translationKeyOf("option", key);
        this.enabledText = Text.translatable(translationKey + "." + enabledKey);
        this.disabledText = Text.translatable(translationKey + "." + disabledKey);
    }

    public CustomBooleanConfigOption(String key, boolean defaultValue) {
        this(key, defaultValue, "true", "false");
    }

    @Override
    public SimpleOption<Boolean> asOption() {
        SimpleOption<Boolean> option = super.asOption();

        if (option.toString().equals("Base Places Entity")) {
            return new SimpleOption<>(translationKey, SimpleOption.constantTooltip(BASE_PLACES_ENTITY_TOOLTIP),
                    (text, value) -> value ? enabledText : disabledText, SimpleOption.BOOLEAN, getValue(),
                    newValue -> ConfigOptionStorage.setBoolean(super.getKey(), newValue));
        }
        else if (option.toString().equals("Base Places Block")) {
            return new SimpleOption<>(translationKey, SimpleOption.constantTooltip(BASE_PLACES_BLOCK_TOOLTIP),
                    (text, value) -> value ? enabledText : disabledText, SimpleOption.BOOLEAN, getValue(),
                    newValue -> ConfigOptionStorage.setBoolean(super.getKey(), newValue));
        }
        return SimpleOption.ofBoolean(translationKey, getValue(), (value) -> ConfigOptionStorage.setBoolean(super.getKey(), value));
    }
}
