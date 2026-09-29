package com.chechotkin.backend.auth.usecase;

public interface CodeNotifier {
    void send(String email, String code);
}
