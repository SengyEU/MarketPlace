package cz.sengycraft.marketplace.utils;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

public class ItemStackUtils {

    public static Component getItemName(ItemStack itemStack) {
        return Optional.ofNullable(itemStack.getItemMeta().displayName())
                .orElse(Component.translatable(itemStack.getType()));
    }

    public static Pair<HashSet<Integer>, ItemStack> getItemFromConfiguration(Section section) {

        String material = Optional.ofNullable(section.getString("material")).orElse("barrier");

        String title = Optional.ofNullable(section.getString("title")).orElse(" ");
        List<String> lore = Optional.ofNullable(section.getStringList("lore")).orElse(new ArrayList<>());
        int customModelData = Optional.ofNullable(section.getInt("custom-model-data")).orElse(0);

        ItemStack itemStack = new ItemStack(Material.valueOf(material.toUpperCase()));

        if(!itemStack.getType().isAir()) {
            ItemMeta itemMeta = itemStack.getItemMeta();
            itemMeta.displayName(ComponentUtils.deserialize(title));
            itemMeta.lore(ComponentUtils.deserialize(lore));
            itemMeta.setCustomModelData(customModelData);

            itemStack.setItemMeta(itemMeta);
        }

        return new Pair<>(getSlots(section.getString("slot")), itemStack);
    }

    public static HashSet<Integer> getSlots(String slot) {

        HashSet<Integer> slots = new HashSet<>();

        String[] parts = slot.split(";");

        for (String part : parts) {

            if (part.contains("-")) {
                String[] range = part.split("-");
                if (range.length == 2 && NumberUtils.isInteger(range[0]) && NumberUtils.isInteger(range[1])) {
                    int start = Integer.parseInt(range[0]);
                    int end = Integer.parseInt(range[1]);

                    if (start <= end) {
                        for (int i = start; i <= end; i++) {
                            slots.add(i);
                        }
                    } else {
                        for (int i = start; i >= end; i--) {
                            slots.add(i);
                        }
                    }
                }
            } else if (NumberUtils.isInteger(part)) {
                slots.add(Integer.parseInt(part));
            }
        }

        return slots;
    }
}
