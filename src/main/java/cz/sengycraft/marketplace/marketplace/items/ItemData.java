package cz.sengycraft.marketplace.marketplace.items;

public class ItemData {

    private String objectId;
    private byte[] item;
    private int price;
    private double halfPrice;
    private int doublePrice;
    private String seller;

    public ItemData(byte[] item, int price, String seller) {
        this.item = item;
        this.price = price;
        this.seller = seller;
        halfPrice = (double) price / 2;
        doublePrice = price * 2;
    }

    public ItemData(String objectId, byte[] item, int price, String seller) {
        this.objectId = objectId;
        this.item = item;
        this.price = price;
        this.seller = seller;
        halfPrice = (double) price / 2;
        doublePrice = price * 2;
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

    public String getHalfPriceFormatted() {
        if (halfPrice == (long) halfPrice)
            return String.format("%d", (long) halfPrice);
        else
            return String.format("%s", halfPrice);
    }

    public double getHalfPrice() {
        return halfPrice;
    }

    public int getDoublePrice() {
        return doublePrice;
    }
}
