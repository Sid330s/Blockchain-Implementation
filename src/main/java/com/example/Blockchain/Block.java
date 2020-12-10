package com.example.Blockchain;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class Block {



    private int index;
    private String timeStamp;
    Transaction transaction;



    private String hash;
    private String previousHash;


    private int nonce;



    public Block(int index, String timeStamp, String previousHash, int nonce,Transaction transaction, String hash) {
        this.index = index;
        this.timeStamp = timeStamp;
        this.previousHash = previousHash;
        this.nonce = nonce;
        this.hash = hash;
        this.transaction = transaction;
    }

    public int getIndex() {
        return index;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public String getPreviousHash() {
        return previousHash;
    }

    public int getNonce() {
        return nonce;
    }

    public void setNonce(int nonce) {
        this.nonce = nonce;
    }

    public static String computeHash(Block block){

        GsonBuilder builder = new GsonBuilder();
        builder.setPrettyPrinting();
        Gson gson = builder.create();

        return Hash.getSHAString(gson.toJson(block));
    }

}
