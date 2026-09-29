package com.chechotkin.backend.auth.notifier;

import com.chechotkin.backend.auth.usecase.CodeNotifier;

import java.util.ArrayList;
import java.util.List;

public class CodeNotifierFake implements CodeNotifier {

    public record Sent(String email, String code){}

    private final List<Sent> sent = new ArrayList<>();

    private RuntimeException failure;


    @Override
    public void send(String email, String code) {
        if(failure!=null){
            throw failure;
        }
        sent.add(new Sent(email, code));
    }
    public List<Sent> sent(){
        return sent;
    }
    public Sent lastSent(){
        return sent.get(sent.size() - 1);
    }
    public void failWith(RuntimeException failure){
        this.failure = failure;
    }
}
