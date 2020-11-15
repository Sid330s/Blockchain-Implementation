import java.text.SimpleDateFormat;
import java.util.Date;

public class Main {
    public static void main(String[] args) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");

        Blockchain blockchain = new Blockchain();
        System.out.println("Hello");
        blockchain.createGenesisBlock();
        blockchain.addNewTransaction(new Transaction("T2"));
        blockchain.mine(2);



    }
}
