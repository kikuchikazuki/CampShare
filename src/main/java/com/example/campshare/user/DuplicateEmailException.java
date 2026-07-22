package com.example.campshare.user;

public class DuplicateEmailException extends RuntimeException {

    public DuplicateEmailException() {
        super("Email is already registered");
    }
}
