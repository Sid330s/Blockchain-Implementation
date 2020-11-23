package com.example.Blockchain;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Main {
    public static void main(String[] args) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");

        Blockchain blockchain = new Blockchain();
        System.out.println(blockchain.getChain().size());
        System.out.println("Hello");
        blockchain.createGenesisBlock();
        System.out.println(blockchain.getChain().size());
        blockchain.addNewTransaction(new Transaction("T2"));
        boolean ans = blockchain.mine(2);
        if(ans) System.out.println("Yes");
        else System.out.println("No");
        System.out.println(blockchain.getChain().size());


    }
}
