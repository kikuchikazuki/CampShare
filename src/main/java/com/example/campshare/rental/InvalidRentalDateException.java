package com.example.campshare.rental;

public class InvalidRentalDateException extends RuntimeException {
    public InvalidRentalDateException() {
        super("終了日は開始日より後の日付を選択してください。");
    }
}
