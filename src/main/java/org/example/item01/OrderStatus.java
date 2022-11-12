package org.example.item01;

public enum OrderStatus {
    PREPARING(0),
    SHIPPED(1),
    DELIVERING(2),
    DELIVERED(3);

    protected int number;

    OrderStatus(int number) {
        this.number = number;
    }

    public int getNumber() {
        return number;
    }
}
