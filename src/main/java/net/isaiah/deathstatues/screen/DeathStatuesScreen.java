package net.isaiah.deathstatues.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;

public class DeathStatuesScreen extends HandledScreen<DeathStatuesScreenHandler> {
    private static final Identifier TEXTURE = new Identifier("minecraft", "textures/gui/container/shulker_box.png");
    public static int deathStatueEntityID = 0;

    public DeathStatuesScreen(DeathStatuesScreenHandler handler, PlayerInventory playerInventory, Text title) {
        super(handler, playerInventory, title);
    }

    @Override
    protected void init() {
        super.init();
        titleY = 1000;
        playerInventoryTitleY = 1000;
    }

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexProgram);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, TEXTURE);
        int x = (width - backgroundWidth) / 2;
        int y = (height - backgroundHeight) / 2;

        context.drawTexture(TEXTURE, x, y, 0, 0, backgroundWidth, backgroundHeight);

        World world = this.handler.blockEntity.getWorld();
        assert world != null;
        LivingEntity deathStatueEntity = (LivingEntity) world.getEntityById(getDeathStatueEntityID());
        if (deathStatueEntity != null) {
            InventoryScreen.drawEntity(context, this.width / 2 - 125, this.height / 2, 20, ((float) this.width / 2 - 125) - mouseX, ((float) this.height / 2) - mouseY, deathStatueEntity);
        }
    }

    public static void setDeathStatueEntityID(int deathStatueEntityID) {
        DeathStatuesScreen.deathStatueEntityID = deathStatueEntityID;
    }

    public static int getDeathStatueEntityID() {
        return deathStatueEntityID;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context);
        super.render(context, mouseX, mouseY, delta);
        drawMouseoverTooltip(context, mouseX, mouseY);
    }
}
