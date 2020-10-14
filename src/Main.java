import java.text.SimpleDateFormat;
import java.util.Date;

public class Main {
    public static void main(String[] args) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        Block block0 = new Block(0,dateFormat.format(new Date()),"Genesis",0);
        Block block1 = new Block(1,dateFormat.format(new Date()),Block.computeHash(block0),2);
        System.out.println(block1.getPreviousHash());
    }
}
