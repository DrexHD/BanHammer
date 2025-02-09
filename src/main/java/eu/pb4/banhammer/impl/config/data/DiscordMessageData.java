package eu.pb4.banhammer.impl.config.data;

import club.minnced.discord.webhook.send.WebhookEmbed;
import club.minnced.discord.webhook.send.WebhookEmbedBuilder;
import club.minnced.discord.webhook.send.WebhookMessage;
import club.minnced.discord.webhook.send.WebhookMessageBuilder;
import net.minecraft.text.TextColor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class DiscordMessageData {
    public boolean sendBanMessage = true;
    public Message banMessage = new Message(
            "red",
            List.of(
                    "${banned} has been banned!",
                    "",
                    "**Reason**: ${reason}",
                    "**By**: ${operator}"
            )
    );

    public boolean sendTempBanMessage = true;
    public Message tempBanMessage = new Message(
            "red",
            List.of(
                    "${banned} has been banned!",
                    "",
                    "**Reason**: ${reason}",
                    "**Expires in**: ${expiration_time}",
                    "**By**: ${operator}"
            )
    );

    public boolean sendBanIpMessage = true;
    public Message banIpMessage = new Message(
            "red",
            List.of(
                    "${banned} has been banned!",
                    "",
                    "**Reason**: ${reason}",
                    "**By**: ${operator}"
            )
    );

    public boolean sendTempBanIpMessage = true;
    public Message tempBanIpMessage = new Message(
            "red",
            List.of(
                    "${banned} has been banned!",
                    "",
                    "**Reason**: ${reason}",
                    "**Expires in**: ${expiration_time}",
                    "**By**: ${operator}"
            )
    );

    public boolean sendMuteMessage = true;
    public Message muteMessage = new Message(
            "red",
            List.of(
                    "${banned} has been muted!",
                    "",
                    "**Reason**: ${reason}",
                    "**By**: ${operator}"
            )
    );

    public boolean sendTempMuteMessage = true;
    public Message tempMuteMessage = new Message(
            "red",
            List.of(
                    "${banned} has been muted!",
                    "",
                    "**Reason**: ${reason}",
                    "**Expires in**: ${expiration_time}",
                    "**By**: ${operator}"
            )
    );

    public boolean sendKickMessage = true;
    public Message kickMessage = new Message(
            "orange",
            List.of(
                    "${banned} has been kicked!",
                    "",
                    "**Reason**: ${reason}",
                    "**By**: ${operator}"
            )
    );

    public boolean sendWarnMessage = true;
    public Message warnMessage = new Message(
            "red",
            List.of(
                    "${banned} has been warned!",
                    "",
                    "**Reason**: ${reason}",
                    "**By**: ${operator}"
            )
    );

    public boolean sendTempWarnMessage = true;
    public Message tempWarnMessage = new Message(
            "red",
            List.of(
                    "${banned} has been warned!",
                    "",
                    "**Reason**: ${reason}",
                    "**Expires in**: ${expiration_time}",
                    "**By**: ${operator}"
            )
    );

    public boolean sendUnbanMessage = true;
    public Message unbanMessage = new Message(
            "green",
            List.of(
                    "${banned} has been unbanned!",
                    "",
                    "**Reason**: ${reason}",
                    "**By**: ${operator}"
            )
    );

    public boolean sendUnbanIpMessage = true;
    public Message unBanIpMessage = new Message(
            "green",
            List.of(
                    "${banned} has been unbanned!",
                    "",
                    "**Reason**: ${reason}",
                    "**By**: ${operator}"
            )
    );

    public boolean sendUnmuteMessage = true;
    public Message unmuteMessage = new Message(
            "green",
            List.of(
                    "${banned} has been unmuted!",
                    "",
                    "**Reason**: ${reason}",
                    "**By**: ${operator}"
            )
    );

    public boolean sendUnwarnMessage = true;
    public Message unwarnMessage = new Message(
            "green",
            List.of(
                    "${banned}'s warnings has been removed!",
                    "",
                    "**Reason**: ${reason}",
                    "**By**: ${operator}"
            )
    );

    public boolean sendPardonMessage = true;
    public Message pardonMessage = new Message(
            "green",
            List.of(
                    "Punishments of player ${banned} has been redeemed!",
                    "",
                    "**Reason**: ${reason}",
                    "**By**: ${operator}"
            )
    );

    public static class Message {
        public boolean embed = true;
        public List<String> message = Collections.emptyList();
        public String avatar = "";
        public String name = "";
        public String embedAuthor = "";
        public String embedAuthorUrl = "";
        public String embedAuthorIconUrl = "";
        public String embedTitle = "";
        public String embedTitleUrl = "";
        public String embedColor = "";
        public String embedImage = "";
        public String embedThumbnail = "";
        public String embedFooter = "";
        public String embedFooterIconUrl = "";
        public List<String> embedMessage = Collections.emptyList();
        public List<Table> embedFields = Collections.emptyList();

        public Message() {
        }

        public Message(String color, List<String> message) {
            this.embedColor = color;
            this.embedMessage = message;
        }

        public WebhookMessage build(Map<String, String> placeholders) {
            WebhookMessageBuilder builder = new WebhookMessageBuilder();
            String name = this.name;
            String avatar = this.avatar;
            String content = String.join("\n", this.message);

            for (Map.Entry<String, String> entry : placeholders.entrySet()) {
                String key = "${" + entry.getKey() + "}";
                name = name.replace(key, entry.getValue());
                avatar = avatar.replace(key, entry.getValue());
                content = content.replace(key, entry.getValue());
            }
            if (!content.isEmpty()) {
                builder.setContent(content);
            }
            if (!avatar.isEmpty()) {
                builder.setAvatarUrl(avatar);
            }
            if (!name.isEmpty()) {
                builder.setUsername(name);
            }

            if (this.embed) {
                String author = this.embedAuthor;
                String authorUrl = this.embedAuthorUrl;
                String authorIconUrl = this.embedAuthorIconUrl;
                String title = this.embedTitle;
                String titleUrl = this.embedTitleUrl;
                String image = this.embedImage;
                String thumbnail = this.embedThumbnail;
                String footer = this.embedFooter;
                String footerIconUrl = this.embedFooterIconUrl;
                String contentEmbed = String.join("\n", this.embedMessage);

                TextColor color = TextColor.parse(this.embedColor);

                List<Table> tables = new ArrayList<>();
                for (Table table : this.embedFields) {
                    tables.add(new Table(table.name, table.content, table.inline));
                }

                for (Map.Entry<String, String> entry : placeholders.entrySet()) {
                    String key = "${" + entry.getKey() + "}";
                    author = author.replace(key, entry.getValue());
                    authorUrl = authorUrl.replace(key, entry.getValue());
                    authorIconUrl = authorIconUrl.replace(key, entry.getValue());
                    title = title.replace(key, entry.getValue());
                    titleUrl = titleUrl.replace(key, entry.getValue());
                    image = image.replace(key, entry.getValue());
                    thumbnail = thumbnail.replace(key, entry.getValue());
                    footer = footer.replace(key, entry.getValue());
                    footerIconUrl = footerIconUrl.replace(key, entry.getValue());
                    contentEmbed = contentEmbed.replace(key, entry.getValue());

                    for (Table table : tables) {
                        table.name = table.name.replace(key, entry.getValue());
                        table.content = table.content.replace(key, entry.getValue());
                    }
                }

                WebhookEmbedBuilder embed = new WebhookEmbedBuilder();

                if (!author.isEmpty()) {
                    embed.setAuthor(new WebhookEmbed.EmbedAuthor(author, authorIconUrl.isEmpty() ? null : authorIconUrl, authorUrl.isEmpty() ? null : authorUrl));
                }
                if (!title.isEmpty()) {
                    embed.setTitle(new WebhookEmbed.EmbedTitle(title, titleUrl.isEmpty() ? null : titleUrl));
                }
                if (!image.isEmpty()) {
                    embed.setImageUrl(image);
                }
                if (!thumbnail.isEmpty()) {
                    embed.setThumbnailUrl(thumbnail);
                }
                if (!footer.isEmpty()) {
                    embed.setFooter(new WebhookEmbed.EmbedFooter(footer, footerIconUrl.isEmpty() ? null : footerIconUrl));
                }

                if (color != null) {
                    embed.setColor(color.getRgb());
                }
                if (!contentEmbed.isEmpty()) {
                    embed.setDescription(contentEmbed);
                }

                for (Table table : tables) {
                    embed.addField(new WebhookEmbed.EmbedField(table.inline, table.name, table.content));
                }

                builder.addEmbeds(embed.build());
            }

            return builder.build();
        }


        private static class Table {
            public String name = "";
            public String content = "";
            public boolean inline = false;

            public Table() {
            }

            public Table(String name, String content, boolean inline) {
                this.name = name;
                this.content = content;
                this.inline = inline;
            }
        }
    }

}
