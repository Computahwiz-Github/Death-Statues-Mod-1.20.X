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

import java.util.List;
import java.util.Optional;

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

    @Override
    protected void init() {
        this.list = new OptionListWidget(this.client, this.width, this.height, 32, this.height - 32, 25); //l = this.height - 32
        this.list.addAll(DeathStatueConfig.asOptions());

        this.addSelectableChild(this.list);
        Text testButtonText = Text.translatable("option.modmenu.base_places_nothing").append(Text.of(": "))
                .append(!DeathStatueConfig.BASE_PLACES_ENTITY.getValue() && !DeathStatueConfig.BASE_PLACES_BLOCK.getValue() ?
                        Text.translatable("option.modmenu.base_places_nothing.true") : Text.translatable("option.modmenu.base_places_nothing.false"));
        testButton = ButtonWidget.builder(testButtonText, (button) -> {
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
            DeathStatueConfig.BASE_PLACES_ENTITY.setValue(DeathStatueConfig.BASE_PLACES_ENTITY.getDefaultValue());
            DeathStatueConfig.BASE_PLACES_BLOCK.setValue(DeathStatueConfig.BASE_PLACES_BLOCK.getDefaultValue());
            DeathStatueConfigManager.save();
            assert client != null;
            client.setScreen(this);
        }).position(this.width / 2 + 15, this.height - 27)
                .size(150, 20)
                .tooltip(Tooltip.of(Text.translatable("option.modmenu.reset.tooltip")))
                .build();

        //addDrawableChild(testButton);
        addDrawableChild(doneButton);
        addDrawableChild(resetButton);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackgroundTexture(context);
        this.list.render(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 5, 0xffffff);
        context.drawCenteredTextWithShadow(this.textRenderer, Text.translatable("option.modmenu.base_places_nothing").append(Text.of(": "))
                        .append(!DeathStatueConfig.BASE_PLACES_ENTITY.getValue() && !DeathStatueConfig.BASE_PLACES_BLOCK.getValue() ?
                                Text.translatable("option.modmenu.base_places_nothing.true") : Text.translatable("option.modmenu.base_places_nothing.false")),this.width / 2, 80, 0xffffff);
        context.drawCenteredTextWithShadow(this.textRenderer, Text.translatable("option.modmenu.base_places_explanation.1"),this.width / 2, 100, 0xffffff);
        context.drawCenteredTextWithShadow(this.textRenderer, Text.translatable("option.modmenu.base_places_explanation.2"),this.width / 2, 115, 0xffffff);
        context.drawCenteredTextWithShadow(this.textRenderer, Text.translatable("option.modmenu.base_places_explanation.3"),this.width / 2, 130, 0xffffff);
        context.drawCenteredTextWithShadow(this.textRenderer, Text.translatable("option.modmenu.base_places_explanation.4"),this.width / 2, 145, 0xffffff);
        placeTooltipOverCenteredText(context, mouseX, mouseY);
        super.render(context, mouseX, mouseY, delta);
    }

    public void placeTooltipOverCenteredText(DrawContext context, int mouseX, int mouseY) {
        boolean mouseXOverBasePlaceIndicatorMaximized;
        boolean mouseXOverBasePlaceIndicatorNotMaximized;
        boolean mouseYOverBasePlaceIndicator = 78 <= mouseY && mouseY <= 90;
        assert client != null;
        if (client.options.getGuiScale().getValue() == 3) {
            mouseXOverBasePlaceIndicatorMaximized = 180 <= (mouseX / 2) && (mouseX / 2) <= 250 && (180 <= (this.width / 4));
            mouseXOverBasePlaceIndicatorNotMaximized = 70 <= (mouseX / 2) && (mouseX / 2) <= 145 && (this.width / 4 <= 145);
            testTooltipPlacement(context, mouseX, mouseY, mouseXOverBasePlaceIndicatorMaximized, mouseXOverBasePlaceIndicatorNotMaximized, mouseYOverBasePlaceIndicator);
        }
        //Finish getting mouse values for different GUI scales
        else if (client.options.getGuiScale().getValue() == 2) {
        }
    }

    public void testTooltipPlacement(DrawContext context, int mouseX, int mouseY, boolean mouseXOverBasePlaceIndicatorMaximized, boolean mouseXOverBasePlaceIndicatorNotMaximized, boolean mouseYOverBasePlaceIndicator) {
        if ((mouseXOverBasePlaceIndicatorMaximized ^ mouseXOverBasePlaceIndicatorNotMaximized) && mouseYOverBasePlaceIndicator) {
            context.drawTooltip(this.textRenderer, List.of(
                    Text.translatable("option.modmenu.base_places_explanation.1"),
                    Text.translatable("option.modmenu.base_places_explanation.2"),
                    Text.translatable("option.modmenu.base_places_explanation.3"),
                    Text.translatable("option.modmenu.base_places_explanation.4")),
                    Optional.empty(), mouseX, mouseY);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.list.getHoveredWidget(mouseX, mouseY).isPresent()) {
            if (this.list.mouseClicked(mouseX, mouseY, button) && this.list.getHoveredWidget(mouseX, mouseY).get().getMessage().contains(Text.translatable("option.modmenu.base_places_entity"))) {
                System.out.println("Base Places Entity Button Clicked");
                if (DeathStatueConfig.BASE_PLACES_ENTITY.getValue() && DeathStatueConfig.BASE_PLACES_BLOCK.getValue()) {
                    DeathStatueConfig.BASE_PLACES_BLOCK.toggleValue();
                }
                assert client != null;
                client.setScreen(this);
                return true;
            }
            else if (this.list.mouseClicked(mouseX, mouseY, button) && this.list.getHoveredWidget(mouseX, mouseY).get().getMessage().contains(Text.translatable("option.modmenu.base_places_block"))) {
                DeathStatueConfig.BASE_PLACES_BLOCK.toggleValue();
                System.out.println("Base Places Block Button Clicked");
                if (DeathStatueConfig.BASE_PLACES_BLOCK.getValue() && DeathStatueConfig.BASE_PLACES_ENTITY.getValue()) {
                    DeathStatueConfig.BASE_PLACES_ENTITY.toggleValue();
                }
                assert client != null;
                client.setScreen(this);
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
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
        System.out.println("Saving Config Options For: " + DeathStatues.MOD_ID);
    }

    @Override
    public void removed() {
        DeathStatueConfigManager.save();
        assert client != null;
        if (client.world != null) {
            ClientPlayNetworking.send(ModMessages.BASE_PLACES_ENTITY_CONFIG_ID, new PacketByteBuf(Unpooled.buffer().writeBoolean(DeathStatueConfig.BASE_PLACES_ENTITY.getValue())));
            ClientPlayNetworking.send(ModMessages.BASE_PLACES_BLOCK_CONFIG_ID, new PacketByteBuf(Unpooled.buffer().writeBoolean(DeathStatueConfig.BASE_PLACES_BLOCK.getValue())));
        }
    }
}
