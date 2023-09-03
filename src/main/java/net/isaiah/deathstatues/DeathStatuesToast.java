package net.isaiah.deathstatues;

import com.google.common.collect.ImmutableList;
import java.util.List;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.toast.Toast;
import net.minecraft.client.toast.ToastManager;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.Nullable;

@Environment(value=EnvType.CLIENT)
public class DeathStatuesToast implements Toast {
    //private static final Text TITLE = Text.translatable("deathstatues.toast.custom_title");
    //private static final Text DESCRIPTION = Text.translatable("deathstatues.toast.custom_description");
    private final Type type;
    private final Text title;
    private final List<OrderedText> lines;
    private long startTime;
    private boolean justUpdated;
    private final int width;

    public DeathStatuesToast(Type type, Text title, @Nullable Text description) {
        this(type, title, getTextAsList(description), Math.max(160, 30 + Math.max(MinecraftClient.getInstance().textRenderer.getWidth(title), description == null ? 0 : MinecraftClient.getInstance().textRenderer.getWidth(description))));
    }

    private DeathStatuesToast(Type type, Text title, List<OrderedText> lines, int width) {
        this.type = type;
        this.title = title;
        this.lines = lines;
        this.width = width;
    }

    private static ImmutableList<OrderedText> getTextAsList(@Nullable Text text) {
        return text == null ? ImmutableList.of() : ImmutableList.of(text.asOrderedText());
    }

    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return 20 + Math.max(this.lines.size(), 1) * 12;
    }

    public Toast.Visibility draw(DrawContext context, ToastManager manager, long startTime) {
        if (this.justUpdated) {
            this.startTime = startTime;
            this.justUpdated = false;
        }

        int i = this.getWidth();
        int j;
        if (i == 160 && this.lines.size() <= 1) {
            context.drawTexture(TEXTURE, 0, 0, 0, 64, i, this.getHeight());
        } else {
            j = this.getHeight();
            int l = Math.min(4, j - 28);
            this.drawPart(context, i, 0, 0, 28);

            for(int m = 28; m < j - l; m += 10) {
                this.drawPart(context, i, 16, m, Math.min(16, j - m - l));
            }

            this.drawPart(context, i, 32 - l, j - l, l);
        }

        if (this.lines == null) {
            context.drawText(manager.getClient().textRenderer, this.title, 18, 12, -256, false);
        } else {
            context.drawText(manager.getClient().textRenderer, this.title, 18, 7, -256, false);

            for(j = 0; j < this.lines.size(); ++j) {
                context.drawText(manager.getClient().textRenderer, this.lines.get(j), 18, 18 + j * 12, -1, false);
            }
        }

        return (double)(startTime - this.startTime) < (double)this.type.displayDuration * manager.getNotificationDisplayTimeMultiplier() ? Visibility.SHOW : Visibility.HIDE;
    }

    private void drawPart(DrawContext context, int width, int textureV, int y, int height) {
        int i = textureV == 0 ? 20 : 5;
        int j = Math.min(60, width - i);
        context.drawTexture(TEXTURE, 0, y, 0, 64 + textureV, i, height);

        for(int k = i; k < width - j; k += 64) {
            context.drawTexture(TEXTURE, k, y, 32, 64 + textureV, Math.min(64, width - k - j), height);
        }

        context.drawTexture(TEXTURE, width - j, y, 160 - j, 64 + textureV, j, height);
    }

    public Type getType() {
        return this.type;
    }

    public static void add(ToastManager manager, Type type, Text title, @Nullable Text description) {
        manager.add(new DeathStatuesToast(type, title, description));
    }

    public static void addDestroyedStatueToast(MinecraftClient client) {
        add(client.getToastManager(), Type.PERIODIC_NOTIFICATION, Text.literal("Death Statues Mod").formatted(Formatting.DARK_PURPLE).formatted(Formatting.BOLD).formatted(Formatting.UNDERLINE), Text.literal("Your Death Statue has been Destroyed!").formatted(Formatting.GOLD));
    }

    /* Where Text.literal() is used to be Text.translatable to use the translation keys from the lang file
    *  However, it acts as a Literal and doesn't register the toast keys. Only translates to the literal key.
    *  Take for example Text.Translatable("deathstatues.toast.custom_title") only outputs "deathstatues.toast.custom_title" not the translated key: "Death Statues Mod"
    *  Any help here would be greatly appreciated.
    * */

    public static void addSpawnedStatueToast(MinecraftClient client) {
        add(client.getToastManager(), Type.PERIODIC_NOTIFICATION, Text.literal("Death Statues Mod"), Text.literal("Your Death Statue has been Created!"));
    }

    public static void addLoadedClientToast(MinecraftClient client) {
        add(client.getToastManager(), Type.PERIODIC_NOTIFICATION, Text.literal("Death Statues Mod"), Text.literal("Welcome & Thanks for downloading!"));
    }

    @Environment(EnvType.CLIENT)
    public enum Type {
        PERIODIC_NOTIFICATION;

        final long displayDuration;

        Type(long displayDuration) {
            this.displayDuration = displayDuration;
        }

        Type() {
            this(4000L);
        }
    }
}

