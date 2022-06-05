package eu.pb4.banhammer.mixin;

import eu.pb4.banhammer.impl.BanHammerImpl;
import eu.pb4.banhammer.impl.config.ConfigManager;
import eu.pb4.banhammer.api.PunishmentType;
import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket;
import net.minecraft.network.packet.c2s.play.CommandExecutionC2SPacket;
import net.minecraft.server.filter.FilteredMessage;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayNetworkHandler.class)
public class ServerPlayNetworkHandlerMixin {
    @Shadow public ServerPlayerEntity player;

    @Inject(method = "handleMessage", at = @At("HEAD"), cancellable = true)
    private void banHammer_checkIfMuted(ChatMessageC2SPacket packet, FilteredMessage<String> message, CallbackInfo ci) {
        var punishments = BanHammerImpl.getPlayersPunishments(this.player.getUuid().toString(), PunishmentType.MUTE);

        if (punishments.size() > 0) {
            var punishment = punishments.get(0);
            this.player.sendMessage(punishment.getDisconnectMessage(), false);
            ci.cancel();
        }
    }

    @Inject(method = "onCommandExecution", at = @At("HEAD"), cancellable = true)
    private void banHammer_checkIfMutedCommand(CommandExecutionC2SPacket packet, CallbackInfo ci) {
        var punishments = BanHammerImpl.getPlayersPunishments(this.player.getUuid().toString(), PunishmentType.MUTE);
        var string = packet.command();
        if (punishments.size() > 0) {
            var punishment = punishments.get(0);

            int x = string.indexOf(" ");
            String rawCommand = string.substring(1, x != -1 ? x : string.length());
            for (String command : ConfigManager.getConfig().mutedCommands) {
                if (rawCommand.startsWith(command)) {
                    ci.cancel();
                    this.player.sendMessage(punishment.getDisconnectMessage(), false);
                    return;
                }
            }
        }
    }
}
