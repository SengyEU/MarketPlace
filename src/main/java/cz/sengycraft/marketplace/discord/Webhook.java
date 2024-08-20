package cz.sengycraft.marketplace.discord;

import club.minnced.discord.webhook.WebhookClient;
import club.minnced.discord.webhook.WebhookClientBuilder;
import cz.sengycraft.marketplace.configuration.ConfigurationManager;
import dev.dejvokep.boostedyaml.YamlDocument;

public class Webhook {

    static ConfigurationManager configurationManager = ConfigurationManager.getInstance();
    static YamlDocument config = configurationManager.getConfiguration("config");

    public static void sendWebhook(String message) {

        if (!config.getBoolean("discord-webhook.enabled")) return;
        String url = config.getString("discord-webhook.url");
        WebhookClient client = new WebhookClientBuilder(url).build();

        client.send(message);
    }
}
