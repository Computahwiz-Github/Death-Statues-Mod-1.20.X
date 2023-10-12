package net.isaiah.deathstatues.mixins;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.isaiah.deathstatues.networking.ModMessages;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.message.SignedMessage;
import net.minecraft.server.command.MessageCommand;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Collection;

@Mixin(MessageCommand.class)
public class MessageCommandMixin {
    @Inject(at = @At("TAIL"), method = "execute")
    private static void getMessage(ServerCommandSource source, Collection<ServerPlayerEntity> targets, SignedMessage message, CallbackInfo ci) {
        PacketByteBuf buf = PacketByteBufs.create();
        String whisperMessage = message.getSignedContent();
        String playerName = source.getName();
        whisperMessage += ","  + playerName;
        //LOGGER.info("Player who sent whisper: " + playerName);
        for (ServerPlayerEntity serverPlayerEntity : targets) {
            ServerPlayNetworking.send(serverPlayerEntity, ModMessages.WHISPER_COMMAND_ID, buf.writeString(whisperMessage));
        }
    }
}
