package com.example.campshare.rental;

public class GearNotFoundException extends RuntimeException {
    public GearNotFoundException(Long gearId) {
        super("用品が見つかりません: " + gearId);
    }
}
