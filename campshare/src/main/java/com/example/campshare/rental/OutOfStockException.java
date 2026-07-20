package com.example.campshare.rental;

public class OutOfStockException extends RuntimeException {
    public OutOfStockException() {
        super("この用品は在庫切れです。");
    }
}
