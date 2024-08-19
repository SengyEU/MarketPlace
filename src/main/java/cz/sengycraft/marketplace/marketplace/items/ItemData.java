package cz.sengycraft.marketplace.marketplace.items;

public class ItemData {

    private String objectId;
    private byte[] item;
    private int price;
    private String seller;

    public ItemData(byte[] item, int price, String seller) {
        this.item = item;
        this.price = price;
        this.seller = seller;
    }

    public ItemData(String objectId, byte[] item, int price, String seller) {
        this.objectId = objectId;
        this.item = item;
        this.price = price;
        this.seller = seller;
    }

    public String getObjectId() {
        return objectId;
    }

    public byte[] getItem() {
        return item;
    }

    public int getPrice() {
        return price;
    }

    public String getSeller() {
        return seller;
    }

}
