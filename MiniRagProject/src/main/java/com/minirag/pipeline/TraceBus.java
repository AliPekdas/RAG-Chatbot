package com.minirag.pipeline;

import com.minirag.model.Context;

// Basit bir loglama yöneticisi (Şimdilik sadece konsola yazsın)
public class TraceBus {
    
    public void publish(String eventName, Context context) {
        // İleride burası JSON dosyasına yazacak.
        // Şimdilik ekrana basıyoruz ki çalıştığını görelim.
        System.out.println("--------------------------------------------------");
        System.out.println("[EVENT]: " + eventName);
        if (context.queryTerms != null && !context.queryTerms.isEmpty()) {
            System.out.println("   -> Terms: " + context.queryTerms);
        }
        if (context.finalAnswer != null) {
            System.out.println("   -> Answer: " + context.finalAnswer);
        }
    }
}