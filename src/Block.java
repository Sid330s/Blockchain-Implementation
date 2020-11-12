import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class Block {

    public int index;
    private String timeStamp;
    private int Transaction;

    public String getPreviousHash() {
        return previousHash;
    }

    public String previousHash;
    public int nonce;
    public String hash;


    public Block(int index, String timeStamp, String previousHash, int nonce, int Transaction) {
        this.index = index;
        this.timeStamp = timeStamp;
        this.previousHash = previousHash;
        this.nonce = nonce;
        this.Transaction = Transaction;
    }

    public static String computeHash(Block block) {

        GsonBuilder builder = new GsonBuilder();
        builder.setPrettyPrinting();
        Gson gson = builder.create();

        return Hash.getSHAString(gson.toJson(block));
    }
}
