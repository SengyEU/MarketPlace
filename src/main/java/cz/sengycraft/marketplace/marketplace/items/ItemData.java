package cz.sengycraft.marketplace.marketplace.items;

import org.bukkit.inventory.ItemStack;

public class ItemData {

    private byte[] item;
    private int price;
    private String seller;

    public ItemData(byte[] item, int price, String seller) {
        this.item = item;
        this.price = price;
        this.seller = seller;
    }

    public byte[] getItem() {
        return item;
    }

    public void setItem(byte[] item) {
        this.item = item;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public String getSeller() {
        return seller;
    }

    public void setSeller(String seller) {
        this.seller = seller;
    }
}
