package cz.sengycraft.marketplace.utils;

import cz.sengycraft.marketplace.configuration.ConfigurationManager;
import dev.dejvokep.boostedyaml.YamlDocument;
import org.bukkit.command.CommandSender;

public class MessageUtils {

    static YamlDocument messages = ConfigurationManager.getInstance().getConfiguration("messages");

    public static void sendMessage(CommandSender sender, String messageKey) {
        sender.sendMessage(ComponentUtils.deserialize(messages.getString(messageKey)));
    }

    @SafeVarargs
    public static void sendMessage(CommandSender sender, String messageKey, Pair<String, String>... placeholders) {

        String message = messages.getString(messageKey);

        for (Pair<String, String> placeholder : placeholders) {
            message = message.replace(placeholder.getLeft(), placeholder.getRight());
        }


        sender.sendMessage(ComponentUtils.deserialize(message));
    }

}
