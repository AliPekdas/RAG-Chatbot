public class TraceBus {
    public void publish(String eventName, Context context) {
        System.out.println("--------------------------------------------------");
        System.out.println("[EVENT]: " + eventName);
        
        if (context.queryTerms != null && !context.queryTerms.isEmpty() && eventName.equals("QueryGenerated")) {
            System.out.println("   -> Terms: " + context.queryTerms);
        }
        
        if (context.hits != null && eventName.equals("Retrieved")) {
            System.out.println("   -> Found Hits: " + context.hits.size());
        }
        
        // GÜNCELLENDİ: Answer Objesini Basma
        if (context.finalAnswer != null && eventName.equals("Answered")) {
            System.out.println("   -> FINAL ANSWER: " + context.finalAnswer.getText());
            System.out.println("   -> CITATIONS: " + context.finalAnswer.getCitations());
        }
    }
}