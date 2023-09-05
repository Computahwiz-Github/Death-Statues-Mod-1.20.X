package net.isaiah.deathstatues;


import com.google.common.collect.ImmutableList;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.toast.Toast;
import net.minecraft.client.toast.ToastManager;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class DeathStatuesToast implements Toast{
    private final Type type;
    private final Text title;
    private final List<OrderedText> descriptionLines;
    private long startTime;
    private boolean justUpdated;
    private final int width;

    public DeathStatuesToast(Type type, Text title, @Nullable Text description) {
        this(type, title, DeathStatuesToast.getTextAsList(description), Math.max(160, 30 + Math.max(MinecraftClient.getInstance().textRenderer.getWidth(title), description == null ? 0 : MinecraftClient.getInstance().textRenderer.getWidth(description))));
    }
    private DeathStatuesToast(Type type, Text title, List<OrderedText> descriptionLines, int width) {
        this.type = type;
        this.title = title;
        this.descriptionLines = descriptionLines;
        this.width = width;
    }

    private static ImmutableList<OrderedText> getTextAsList(@Nullable Text text) {
        return text == null ? ImmutableList.of() : ImmutableList.of(text.asOrderedText());
    }

    @Override
    public int getWidth() {
        return this.width;
    }

    @Override
    public int getHeight() {
        return 20 + Math.max(this.descriptionLines.size(), 1) * 12;
    }

    @Override
    public Toast.Visibility draw(DrawContext context, ToastManager manager, long startTime) {
        int j;
        int i;
        if (this.justUpdated) {
            this.startTime = startTime;
            this.justUpdated = false;
        }
        if ((i = this.getWidth()) == 160 && this.descriptionLines.size() <= 1) {
            context.drawTexture(TEXTURE, 0, 0, 0, 0, i, this.getHeight());
        }
        else {
            j = this.getHeight();
            int l = Math.min(4, j - 32);
            this.drawPart(context, i, 0, 0, 32);
            for (int m = 32; m < j - l; m += 10) {
                this.drawPart(context, i, 16, m, Math.min(16, j - m - l));
            }
            this.drawPart(context, i, 32 - l, j - l, l);
        }
        if (this.descriptionLines == null) {
            context.drawText(manager.getClient().textRenderer, this.title, 30, 12, -256, false);
        }
        else {
            //Centering title depending on description width is
            int titlePosition =((this.getWidth() - this.title.getString().length())/3)-5;
            //Drawing title starting from new calculated position
            context.drawText(manager.getClient().textRenderer, this.title, titlePosition, 7, -256, false);
            for (j = 0; j < this.descriptionLines.size(); ++j) {
                context.drawText(manager.getClient().textRenderer, this.descriptionLines.get(j), 18, 18 + j * 12, -1, false);
            }
        }
        return (double)(startTime - this.startTime) < (double)this.type.displayDuration * manager.getNotificationDisplayTimeMultiplier() ? Toast.Visibility.SHOW : Toast.Visibility.HIDE;
    }

    private void drawPart(DrawContext context, int width, int textureV, int y, int height) {
        int i = textureV == 0 ? 20 : 5;
        int j = Math.min(60, width - i);
        context.drawTexture(TEXTURE, 0, y, 0, 0, i, height);
        for (int k = i; k < width - j; k += 32) {
            context.drawTexture(TEXTURE, k, y, 32, 0, Math.min(32, width - k - j), height);
        }
        context.drawTexture(TEXTURE, width - j, y, 160 - j, 0, j, height);
    }

    public static void add(ToastManager manager, Type type, Text title, @Nullable Text description) {
        manager.add(new DeathStatuesToast(type, title, description));
    }

    @Environment(value= EnvType.CLIENT)
    public enum Type {
        PERIODIC_NOTIFICATION;

        final long displayDuration;

        Type(long displayDuration) {
            this.displayDuration = displayDuration;
        }

        Type() {
            this(5000L);
        }
    }
}

