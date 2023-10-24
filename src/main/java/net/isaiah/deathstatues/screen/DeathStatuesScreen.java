package net.isaiah.deathstatues.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import net.isaiah.deathstatues.entity.deathstatue.DeathStatueEntity;
import net.isaiah.deathstatues.item.ModItems;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DeathStatuesScreen extends HandledScreen<DeathStatuesScreenHandler> {
    private static final Identifier TEXTURE = new Identifier("deathstatues", "textures/gui/container/death_statue_base.png");
    private static final Identifier TEXTURE_WITH_ENTITY = new Identifier("deathstatues", "textures/gui/container/death_statue_base_with_entity.png");
    public static int deathStatueEntityID = 0;
    private ItemStack quickMovingStack = ItemStack.EMPTY;

    public DeathStatuesScreen(DeathStatuesScreenHandler handler, PlayerInventory playerInventory, Text title) {
        super(handler, playerInventory, title);
    }

    @Override
    protected void init() {
        super.init();
        titleY = 1000;
        playerInventoryTitleY = 1000;

        ButtonWidget statueConvertButton;
        statueConvertButton = ButtonWidget.builder(Text.translatable(""), (button) -> {
                    if (this.handler.slots.get(27).getStack().getName().getString().equals("Death Statue Entity")) {
                        this.handler.blockEntity.setStack(27, ItemStack.EMPTY);
                        this.handler.updateToClient();

                    }
                    assert client != null;
                    client.setScreen(this);
                }).position((this.width - backgroundWidth - 28) / 2, (this.height - backgroundHeight + 123) / 2)
                .size(18, 18)
                .tooltip(Tooltip.of(Text.of("Convert Regular Statue Item to One With Full NBT")))
                .build();

        addDrawableChild(statueConvertButton);
    }

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexProgram);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        //RenderSystem.setShaderTexture(0, TEXTURE);
        int x = (width - backgroundWidth) / 2;
        int y = (height - backgroundHeight) / 2;
        int x2 = (width - backgroundWidth - 76 - 74) / 2;
        //context.drawTexture(TEXTURE, x, y, 0, 0, backgroundWidth, backgroundHeight);

        World world = this.handler.blockEntity.getWorld();
        assert world != null;
        LivingEntity deathStatueEntity = (LivingEntity) world.getEntityById(getDeathStatueEntityID());

        if (deathStatueEntity != null && deathStatueEntity.getBlockPos().equals(this.handler.blockEntity.getPos().up())) {
            RenderSystem.setShaderTexture(0, TEXTURE_WITH_ENTITY);
            context.drawTexture(TEXTURE_WITH_ENTITY, x2, y, 0, 0, this.backgroundWidth + 74, backgroundHeight);
            InventoryScreen.drawEntity(context, (this.width) / 2 - 130, this.height / 2 - 15, 25, ((float) this.width / 2 - 130) - mouseX, ((float) this.height / 2) - mouseY, deathStatueEntity);
        }
        else {
            RenderSystem.setShaderTexture(0, TEXTURE);
            context.drawTexture(TEXTURE, x2, y, 0, 0, backgroundWidth + 74, backgroundHeight);
        }
    }

    public ItemStack giveCustomStatueItem() {
        World world = this.handler.blockEntity.getWorld();
        assert world != null;
        DeathStatueEntity deathStatueEntity = (DeathStatueEntity) world.getEntityById(getDeathStatueEntityID());
        ItemStack deathStatueStack = ModItems.DEATH_STATUE_ITEM.getDefaultStack();
        NbtCompound nbtData = new NbtCompound();
        assert deathStatueEntity != null;
        nbtData.put("Inventory", deathStatueEntity.inventory.writeNbt(new NbtList()));
        //System.out.println("NBT DATA: " + nbtData);
        deathStatueStack.setNbt(nbtData);
        String customName = Objects.requireNonNull(deathStatueEntity.getName()).getString();

        Pattern pattern = Pattern.compile("\\[(.*?)]");
        Matcher matcher = pattern.matcher(customName);
        if (matcher.find()) {
            try {
                if (matcher.group(1) != null) {
                    customName = matcher.group(1);
                    deathStatueStack.setCustomName(Text.of("Death Statue of ").copyContentOnly().formatted(Formatting.AQUA)
                            .append(Text.of("[").copyContentOnly().formatted(Formatting.GREEN))
                            .append(Text.of(customName).copyContentOnly().formatted(Formatting.GOLD))
                            .append(Text.of("]").copyContentOnly().formatted(Formatting.GREEN)));
                }
            } catch (IllegalArgumentException e) {
                // UUID parsing failed
                e.printStackTrace();
            }
        }
        return deathStatueStack;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        Slot slot = this.getSlotAt(mouseX, mouseY);
        if (slot != null) {
            if ((Objects.requireNonNull(slot).getIndex() == 27 ^ slot.getIndex() == 28) && this.isClickOutsideBounds(mouseX, mouseY, this.x, this.y, 0)) {
                boolean notOutsideBounds = this.isClickOutsideBounds(mouseX, mouseY, this.x, this.y, button);
                boolean bl3 = (InputUtil.isKeyPressed(MinecraftClient.getInstance().getWindow().getHandle(), GLFW.GLFW_KEY_LEFT_SHIFT) || InputUtil.isKeyPressed(MinecraftClient.getInstance().getWindow().getHandle(), GLFW.GLFW_KEY_RIGHT_SHIFT));
                SlotActionType slotActionType = SlotActionType.PICKUP;
                if (bl3) {
                    this.quickMovingStack = slot.hasStack() ? slot.getStack().copy() : ItemStack.EMPTY;
                    slotActionType = SlotActionType.QUICK_MOVE;
                }
                else if (notOutsideBounds) {
                    if (!this.handler.getCursorStack().isEmpty()) {
                        slotActionType = SlotActionType.PICKUP_ALL;
                    }
                    else {
                        slot.setStack(ItemStack.EMPTY);
                    }
                    //System.out.println("Slot in Base has stack: " + Objects.requireNonNull(slot).hasStack());
                }
                this.onMouseClick(slot, slot.id, button, slotActionType);
            }
        }
        super.mouseClicked(mouseX, mouseY, button);
        return true;
    }

    @Nullable
    private Slot getSlotAt(double x, double y) {
        for (int i = 0; i < this.handler.slots.size(); ++i) {
            Slot slot = this.handler.slots.get(i);
            if (!this.isPointOverSlot(slot, x, y) || !slot.isEnabled()) continue;
            return slot;
        }
        return null;
    }

    private boolean isPointOverSlot(Slot slot, double pointX, double pointY) {
        return this.isPointWithinBounds(slot.x, slot.y, 16, 16, pointX, pointY);
    }

    public static void setDeathStatueEntityID(int deathStatueEntityID) {
        DeathStatuesScreen.deathStatueEntityID = deathStatueEntityID;
    }

    public static int getDeathStatueEntityID() {
        return deathStatueEntityID;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        //renderBackground(context);
        this.renderBackground(context);
        super.render(context, mouseX, mouseY, delta);
        drawMouseoverTooltip(context, mouseX, mouseY);
        context.drawItem(ModItems.DEATH_STATUE_ITEM.getDefaultStack(), (this.width - backgroundWidth - 26) / 2, (this.height - backgroundHeight + 125) / 2);
    }
}
