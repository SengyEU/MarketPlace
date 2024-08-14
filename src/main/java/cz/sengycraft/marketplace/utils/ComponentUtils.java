package cz.sengycraft.marketplace.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.inventory.ItemStack;

import java.util.Optional;

public class ComponentUtils {

    public static Component deserialize(String message) {
        return MiniMessage.miniMessage().deserialize(message);
    }

    public static String serialize(Component component) {
        return MiniMessage.miniMessage().serialize(component);
    }

    public static Component getItemName(ItemStack itemStack) {
        return Optional.ofNullable(itemStack.getItemMeta().displayName())
                .orElse(Component.translatable(itemStack.getType()));
    }

}
