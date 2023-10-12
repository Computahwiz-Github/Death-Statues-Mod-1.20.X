package net.isaiah.deathstatues.screen;

import io.netty.buffer.Unpooled;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.isaiah.deathstatues.DeathStatues;
import net.isaiah.deathstatues.config.DeathStatueConfig;
import net.isaiah.deathstatues.config.DeathStatueConfigManager;
import net.isaiah.deathstatues.networking.ModMessages;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.option.GameOptionsScreen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.*;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenTexts;
import net.minecraft.text.Text;

public class DeathStatuesConfigScreen extends GameOptionsScreen {
    private final Screen parent;
    public DeathStatuesConfigScreen(Screen parent) {
        super(parent, MinecraftClient.getInstance().options, Text.translatable("deathstatues.title"));
        this.parent = parent;
    }
    public ButtonWidget doneButton;
    public ButtonWidget resetButton;
    public ButtonWidget testButton;
    private OptionListWidget list;
    private int shownNothingButton = 0;
    private int shownEntityButton = 0;
    private int shownBlockButton = 0;

    @Override
    protected void init() {
        this.list = new OptionListWidget(this.client, this.width, this.height, 32, this.height - 32, 25);
        this.list.addAll(DeathStatueConfig.asOptions());

        this.addSelectableChild(this.list);
        testButton = ButtonWidget.builder(Text.translatable("option.modmenu.base_places_nothing").append(Text.of(": "))
                        .append(!DeathStatueConfig.BASE_PLACES_ENTITY.getValue() && !DeathStatueConfig.BASE_PLACES_BLOCK.getValue() ?
                                Text.translatable("option.modmenu.base_places_nothing.true") : Text.translatable("option.modmenu.base_places_nothing.false")), (button) -> {
                    assert client != null;
                    client.setScreen(this);
                    System.out.println("Clicked testButton");
                }).position(this.width / 2 - 80, 90)
                .size(165, 20)
                .tooltip(Tooltip.of(Text.translatable("option.modmenu.base_places_nothing.tooltip")))
                .build();

        doneButton = ButtonWidget.builder(ScreenTexts.DONE, (button) -> {
            DeathStatueConfigManager.save();
            assert this.client != null;
            this.client.setScreen(this.parent);
        }).position(this.width / 2 - 165, this.height - 27)
                .size(150, 20)
                .tooltip(Tooltip.of(Text.translatable("option.modmenu.done.tooltip")))
                .build();

        resetButton = ButtonWidget.builder(Text.translatable("option.modmenu.reset.button"), (button) -> {
            //DeathStatueConfig.BASE_PLACES_NOTHING.setValue(DeathStatueConfig.BASE_PLACES_NOTHING.getDefaultValue());
            DeathStatueConfig.BASE_PLACES_ENTITY.setValue(DeathStatueConfig.BASE_PLACES_ENTITY.getDefaultValue());
            DeathStatueConfig.BASE_PLACES_BLOCK.setValue(DeathStatueConfig.BASE_PLACES_BLOCK.getDefaultValue());
            DeathStatueConfigManager.save();
            assert client != null;
            client.setScreen(this);
        }).position(this.width / 2 + 15, this.height - 27)
                .size(150, 20)
                .tooltip(Tooltip.of(Text.translatable("option.modmenu.reset.tooltip")))
                .build();

        addDrawableChild(testButton);
        addDrawableChild(doneButton);
        addDrawableChild(resetButton);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        updateButtons();
        this.renderBackgroundTexture(context);
        this.list.render(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 5, 0xffffff);
        /*context.drawCenteredTextWithShadow(this.textRenderer, Text.translatable("option.modmenu.base_places_nothing").append(Text.of(": "))
                        .append(!DeathStatueConfig.BASE_PLACES_ENTITY.getValue() && !DeathStatueConfig.BASE_PLACES_BLOCK.getValue() ?
                                Text.translatable("option.modmenu.base_places_nothing.true") : Text.translatable("option.modmenu.base_places_nothing.false")),this.width / 2, 100, 0xffffff);*/
        super.render(context, mouseX, mouseY, delta);
    }

    public void updateButtons() {

        if (this.shownNothingButton == 0 && DeathStatueConfig.BASE_PLACES_ENTITY.getValue() == DeathStatueConfig.BASE_PLACES_BLOCK.getValue()) {
            this.shownNothingButton++;
            assert client != null;
            client.setScreen(this);
            this.shownEntityButton = 0;
            this.shownBlockButton = 0;
        }

        if (DeathStatueConfig.BASE_PLACES_BLOCK.getValue()) {
            if (this.shownBlockButton == 0) {
                this.shownBlockButton++;
                assert client != null;
                client.setScreen(this);
                this.shownNothingButton = 0;
                this.shownEntityButton = 0;
            }
            if (DeathStatueConfig.BASE_PLACES_ENTITY.getValue()) {
                DeathStatueConfig.BASE_PLACES_BLOCK.toggleValue();
                DeathStatueConfig.BASE_PLACES_ENTITY.toggleValue();
                assert client != null;
                client.setScreen(this);
                this.shownNothingButton = 0;
                this.shownEntityButton = 0;
                this.shownBlockButton = 0;
            }
        }
        else if (DeathStatueConfig.BASE_PLACES_ENTITY.getValue()) {
            if (this.shownEntityButton == 0) {
                this.shownEntityButton++;
                assert client != null;
                client.setScreen(this);
                this.shownBlockButton = 0;
                this.shownNothingButton = 0;
            }
        }
    }
    @Override
    public void close() {
        DeathStatueConfigManager.save();
        assert client != null;
        if (client.world != null) {
            ClientPlayNetworking.send(ModMessages.BASE_PLACES_ENTITY_CONFIG_ID, new PacketByteBuf(Unpooled.buffer().writeBoolean(DeathStatueConfig.BASE_PLACES_ENTITY.getValue())));
            ClientPlayNetworking.send(ModMessages.BASE_PLACES_BLOCK_CONFIG_ID, new PacketByteBuf(Unpooled.buffer().writeBoolean(DeathStatueConfig.BASE_PLACES_BLOCK.getValue())));
        }
        client.setScreen(parent);
    }

    @Override
    public void removed() {
        DeathStatueConfigManager.save();
        assert client != null;
        if (client.world != null) {
            ClientPlayNetworking.send(ModMessages.BASE_PLACES_ENTITY_CONFIG_ID, new PacketByteBuf(Unpooled.buffer().writeBoolean(DeathStatueConfig.BASE_PLACES_ENTITY.getValue())));
            ClientPlayNetworking.send(ModMessages.BASE_PLACES_BLOCK_CONFIG_ID, new PacketByteBuf(Unpooled.buffer().writeBoolean(DeathStatueConfig.BASE_PLACES_BLOCK.getValue())));
        }
        System.out.println("Saving Config Options For: " + DeathStatues.MOD_ID);
    }
}
