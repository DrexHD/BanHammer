package eu.pb4.banhammer.impl.commands;


import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import eu.pb4.banhammer.api.PunishmentType;
import eu.pb4.banhammer.impl.BHUtils;
import eu.pb4.banhammer.impl.BanHammerImpl;
import eu.pb4.banhammer.impl.config.ConfigManager;
import eu.pb4.banhammer.impl.config.data.DiscordMessageData;
import eu.pb4.placeholders.PlaceholderAPI;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.minecraft.network.MessageType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Util;

import java.util.HashMap;
import java.util.concurrent.CompletableFuture;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class UnpunishCommands {
    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> {
            dispatcher.register(create("unban", PunishmentType.BAN));
            dispatcher.register(create("unban-ip", PunishmentType.IP_BAN));
            dispatcher.register(create("unmute", PunishmentType.MUTE));
            dispatcher.register(create("unwarn", PunishmentType.WARN));
            dispatcher.register(create("pardon", null));
        });
    }

    private static LiteralArgumentBuilder<ServerCommandSource> create(String command, PunishmentType type) {
        return literal(command)
                .requires(ConfigManager.requirePermissionOrOp("banhammer.unpunish." + command))
                .then(GeneralCommands.playerArgument("player")
                        .executes(ctx -> removePunishmentCommand(ctx, type))
                        .then(argument("reason", StringArgumentType.greedyString())
                                .executes(ctx -> removePunishmentCommand(ctx, type))
                        )
                );
    }

    private static int removePunishmentCommand(CommandContext<ServerCommandSource> ctx, PunishmentType type) {
        CompletableFuture.runAsync(() -> {

            var config = ConfigManager.getConfig();

            var playerNameOrIp = ctx.getArgument("player", String.class);
            var players = BHUtils.lookupPlayerData(playerNameOrIp, ctx.getSource().getServer());

            if (players.isEmpty()) {
                ctx.getSource().sendFeedback(new LiteralText("Couldn't find player " + playerNameOrIp + "!").formatted(Formatting.RED), false);
            }

            ServerPlayerEntity executor;
            try {
                executor = ctx.getSource().getPlayer();
            } catch (Exception e) {
                executor = null;
            }

            String reason;
            boolean isSilent;
            try {
                String temp = ctx.getArgument("reason", String.class);
                if (temp.startsWith("-")) {
                    String[] parts = temp.split(" ", 2);

                    isSilent = parts[0].contains("s");
                    reason = parts.length == 2 ? parts[1] : config.defaultReason;
                } else {
                    reason = temp;
                    isSilent = false;
                }

            } catch (Exception e) {
                reason = config.defaultReason;
                isSilent = false;
            }

            for (var player : players) {
                Text message = null;
                String altMessage = "";
                int n = 0;

                if (type != null) {
                    switch (type) {
                        case BAN:
                            n += BanHammerImpl.removePunishment(player.uuid().toString(), PunishmentType.BAN);
                            message = config.unbanChatMessage;
                            altMessage = "This player wasn't banned!";
                            break;
                        case IP_BAN:
                            if (player.ip() != null) {
                                n += BanHammerImpl.removePunishment(player.ip(), PunishmentType.IP_BAN);
                            }
                            if (type == PunishmentType.IP_BAN && ConfigManager.getConfig().configData.standardBanPlayersWithBannedIps) {
                                n += BanHammerImpl.removePunishment(player.uuid().toString(), PunishmentType.BAN);
                            }
                            message = config.ipUnbanChatMessage;
                            altMessage = "This player wasn't ipbanned!";
                            break;
                        case MUTE:
                            n += BanHammerImpl.removePunishment(player.uuid().toString(), PunishmentType.MUTE);
                            message = config.unmuteChatMessage;
                            altMessage = "This player wasn't muted!";
                            break;
                        case WARN:
                            n += BanHammerImpl.removePunishment(player.uuid().toString(), PunishmentType.WARN);
                            message = config.unwarnChatMessage;
                            altMessage = "This player wasn't warned!";
                            break;
                    }
                } else {
                    if (player.uuid() != null) {
                        var uuid = player.uuid().toString();
                        n += BanHammerImpl.removePunishment(uuid, PunishmentType.BAN);
                        n += BanHammerImpl.removePunishment(uuid, PunishmentType.WARN);
                        n += BanHammerImpl.removePunishment(uuid, PunishmentType.MUTE);
                    }
                    if (player.ip() != null) {
                        n += BanHammerImpl.removePunishment(player.ip(), PunishmentType.IP_BAN);
                    }

                    message = config.pardonChatMessage;
                    altMessage = "This player didn't have any punishments!";
                }

                if (n > 0) {
                    HashMap<String, Text> list = new HashMap<>();

                    list.put("operator", ctx.getSource().getDisplayName());
                    list.put("banned", new LiteralText(player.name()));
                    list.put("banned_uuid", new LiteralText(player.uuid().toString()));
                    list.put("reason", new LiteralText(reason));
                    Text textMessage = PlaceholderAPI.parsePredefinedText(message, PlaceholderAPI.PREDEFINED_PLACEHOLDER_PATTERN, list);

                    if (config.configData.punishmentsAreSilent || isSilent) {
                        if (player.player() != null) {
                            player.player().sendMessage(textMessage, false);
                        }

                        ctx.getSource().sendFeedback(textMessage, false);
                    } else {
                        ctx.getSource().sendFeedback(textMessage, false);

                        for (ServerPlayerEntity player2 : ctx.getSource().getServer().getPlayerManager().getPlayerList()) {
                            if (player2 != executor) {
                                player2.sendMessage(textMessage, MessageType.SYSTEM, Util.NIL_UUID);
                            }
                        }
                    }

                    if (config.webhook != null) {
                        DiscordMessageData.Message tempMessage;
                        var data = config.discordMessages;
                        if (type != null) {
                            tempMessage = switch (type) {
                                case BAN -> data.sendUnbanMessage ? data.unbanMessage : null;
                                case IP_BAN -> data.sendUnbanIpMessage ? data.unBanIpMessage : null;
                                case MUTE -> data.sendUnmuteMessage ? data.unmuteMessage : null;
                                case WARN -> data.sendUnwarnMessage ? data.unwarnMessage : null;
                                case KICK -> null;
                            };
                        } else {
                            tempMessage = data.sendPardonMessage ? data.pardonMessage : null;
                        }

                        if (tempMessage != null) {
                            var placeholders = new HashMap<String, String>();

                            placeholders.put("operator", ctx.getSource().getDisplayName().getString());
                            placeholders.put("banned", player.name());
                            placeholders.put("banned_uuid", player.uuid().toString());
                            placeholders.put("reason", reason);

                            config.webhook.send(tempMessage.build(placeholders));
                        }
                    }
                } else {
                    ctx.getSource().sendFeedback(new LiteralText(altMessage).formatted(Formatting.RED), false);
                }
            }
        });

        return 1;
    }


}
