package cz.sengycraft.marketplace.transactions;

import java.time.LocalDateTime;

public class TransactionData {

    private byte[] item;
    private boolean blackmarket;
    private double priceBuyer;
    private int priceSeller;
    private String buyer;
    private String seller;
    private LocalDateTime date;

    public TransactionData(byte[] item, boolean blackmarket, double priceBuyer, int priceSeller, String buyer, String seller, LocalDateTime date) {
        this.item = item;
        this.blackmarket = blackmarket;
        this.priceBuyer = priceBuyer;
        this.priceSeller = priceSeller;
        this.buyer = buyer;
        this.seller = seller;
        this.date = date;
    }

    public byte[] getItem() {
        return item;
    }

    public boolean isBlackmarket() {
        return blackmarket;
    }

    public double getPriceBuyer() {
        return priceBuyer;
    }

    public int getPriceSeller() {
        return priceSeller;
    }

    public String getBuyer() {
        return buyer;
    }

    public String getSeller() {
        return seller;
    }

    public LocalDateTime getDate() {
        return date;
    }
}
