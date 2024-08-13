package cz.sengycraft.marketplace.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

public class ComponentUtils {

    public static Component deserialize(String message){
        return MiniMessage.miniMessage().deserialize(message);
    }

}
