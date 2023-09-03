package net.isaiah.deathstatues;


import net.minecraft.client.MinecraftClient;
import net.minecraft.client.toast.SystemToast;
import net.minecraft.text.LiteralTextContent;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class DeathStatuesToast {
    /*public static void showCustomToast(String title, String[] descriptionLines) {
        MinecraftClient client = MinecraftClient.getInstance();
        LiteralTextContent description = new LiteralTextContent("");
        for (String line : descriptionLines) {
            description.append(line).append("\n");
        }
        SystemToast toast = SystemToast.create(client, SystemToast.Type.TUTORIAL_HINT, new LiteralTextContent(title), description);
        client.getToastManager().add(toast);
    }*/
    public static void showLoadedClientToast(String titleKey, String loadedKey) {
        MinecraftClient client = MinecraftClient.getInstance();
        SystemToast toast = SystemToast.create(client, SystemToast.Type.PERIODIC_NOTIFICATION, Text.translatable(titleKey).formatted(Formatting.BOLD), Text.translatable(loadedKey).formatted(Formatting.DARK_PURPLE));

        client.getToastManager().add(toast);
    }
    public static void showSpawnedStatueToast(String titleKey, String spawnedKey) {
        MinecraftClient client = MinecraftClient.getInstance();
        SystemToast toast = SystemToast.create(client, SystemToast.Type.PERIODIC_NOTIFICATION, Text.translatable(titleKey).formatted(Formatting.BOLD), Text.translatable(spawnedKey).formatted(Formatting.DARK_PURPLE));

        client.getToastManager().add(toast);
    }
    public static void showDestroyedStatueToast(String titleKey, String destroyedKey) {
        MinecraftClient client = MinecraftClient.getInstance();
        SystemToast toast = SystemToast.create(client, SystemToast.Type.PERIODIC_NOTIFICATION, Text.translatable(titleKey).formatted(Formatting.BOLD), Text.translatable(destroyedKey).formatted(Formatting.DARK_PURPLE));

        client.getToastManager().add(toast);
    }
}

