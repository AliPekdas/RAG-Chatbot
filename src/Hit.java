public class Hit {
    public int docId;
    public int chunkId;
    public double score;
    public String body;

    public Hit(int docId, int chunkId, double score, String body) {
        this.docId = docId;
        this.chunkId = chunkId;
        this.score = score;
        this.body = body;
    }

    @Override
    public String toString() {
        return String.format("Doc:%d Chunk:%d Score:%.2f", docId, chunkId, score);
    }
}