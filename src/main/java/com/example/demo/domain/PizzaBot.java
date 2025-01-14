//package com.example.demo.domain;
//
//
//import com.example.demo.model.ResponseHandler;
//import org.hibernate.cfg.Environment;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Component;
//import org.telegram.abilitybots.api.bot.AbilityBot;
//
//@Component
//public class PizzaBot extends AbilityBot {
//    private final ResponseHandler responseHandler;
//    @Autowired
//    public PizzaBot(Environment env) {
//        super(env.getProperty("botToken"), "baeldungbot");
//        responseHandler = new ResponseHandler(silent, db);
//    }
//    @Override
//    public long creatorId() {
//        return 1L;
//    }
//}
