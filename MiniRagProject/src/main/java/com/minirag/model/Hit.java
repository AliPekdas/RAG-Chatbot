package com.minirag.model;

public class Hit {
    private int docId;       // Which Document
    private int chunkId;     // Which part
    private String text;     // Text of The Piece
    private double score;    // Score

    public Hit(int docId, int chunkId, String text, double score) {
        this.docId = docId;
        this.chunkId = chunkId;
        this.text = text;
        this.score = score;
    }

    // Getter and Setter Methods
    public int getDocId() { return docId; }
    public int getChunkId() { return chunkId; }
    public String getText() { return text; }
    public double getScore() { return score; }
    public void setScore(double score) { this.score = score; }

    @Override
    public String toString() {
        return "Hit{doc=" + docId + ", chunk=" + chunkId + ", score=" + score + "}";
    }
}