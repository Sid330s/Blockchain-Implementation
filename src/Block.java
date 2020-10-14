import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class Block {

    private int index;
    private String timeStamp;

    public String getPreviousHash() {
        return previousHash;
    }

    private String previousHash;
    private int nonce;

    public Block(int index, String timeStamp, String previousHash, int nonce) {
        this.index = index;
        this.timeStamp = timeStamp;
        this.previousHash = previousHash;
        this.nonce = nonce;
    }



    public static String computeHash(Block block){

        GsonBuilder builder = new GsonBuilder();
        builder.setPrettyPrinting();
        Gson gson = builder.create();

        return Hash.getSHAString(gson.toJson(block));
    }

}
