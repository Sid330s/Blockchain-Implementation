package com.example.Blockchain;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.squareup.okhttp.*;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

@SpringBootApplication
@RestController
public class BlockchainApplication {
	private static Blockchain blockchain ;
    public static Set<String> peers = new HashSet<>();



	public static void main(String[] args) {

		blockchain = new Blockchain();
		System.out.println("Hello");
		blockchain.createGenesisBlock();
		blockchain.addNewTransaction(new Transaction("T2"));
		boolean ans = blockchain.mine(2);

		SpringApplication.run(BlockchainApplication.class, args);

	}

	@GetMapping("/hello")
	public String hello(@RequestParam(value = "name", defaultValue = "World") String name) {
		return String.format("Hello %s!", name);
	}
    //mvnw spring-boot:run
	@GetMapping("/chain")
	public ChainStatus get_chain() {
        ChainStatus chainStatus = new ChainStatus();
        chainStatus.chain = blockchain.getChain();
        chainStatus.length = blockchain.getChain().size();
		chainStatus.peers = peers;
        return chainStatus;
	}

	@GetMapping("/mine")
	public String mine_unconfirmed_transactions() {
		boolean result = blockchain.mine(0);
		if (result) return "No transactions to mine";

		//Making sure we have the longest chain before announcing to the network

		int chain_length = blockchain.getChain().size();
		try {
			boolean flag = consensus();
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (chain_length == blockchain.getChain().size()) announce_new_block(blockchain.getLastBlock());
		//announce the recently mined block to the network

		return String.format("Block %s is mined.", blockchain.getLastBlock().getIndex());
	}

	/*
    Our naive consensus algorithm. If a longer valid chain is
    found, our chain is replaced with it.
    */
	boolean consensus() throws IOException {

		ArrayList<Block> longest_chain = null;
		int current_len = blockchain.getChain().size();


		for (String node : peers){

			ObjectMapper mapper = new ObjectMapper();
			OkHttpClient client = new OkHttpClient();
			Request request = new Request.Builder()
					.url(String.format("%schain",node))
					.build(); // defaults to GET
			Response response = client.newCall(request).execute();
			ChainStatus chainStatus = mapper.readValue(response.body().byteStream(), ChainStatus.class);
			int length = chainStatus.length;
			ArrayList<Block>chain = chainStatus.chain;
			if (length > current_len && blockchain.checkChainValidity(chain)){
				current_len = length;
				longest_chain = chain;
			}

		}



		if (longest_chain.size()>0){
			blockchain.setChain(longest_chain);
			return true;
		}


		return false;
	}


	/*
    A function to announce to the network once a block has been mined.
    Other blocks can simply verify the proof of work and add it to their
    respective chains.
    */
	void announce_new_block(Block block){
		for (String peer : peers){
			String url = String.format("%sadd_block",peer);

			OkHttpClient client = new OkHttpClient();
			GsonBuilder builder = new GsonBuilder();
			builder.setPrettyPrinting();
			Gson gson = builder.create();
			String requestString = gson.toJson(block);

			RequestBody requestBody = RequestBody.create(
					MediaType.parse("application/json"), requestString);

			Request request = new Request.Builder()
					.url(url)
					.addHeader("Content-Type", "application/json")
					.post(requestBody)
					.build();

			Call call = client.newCall(request);
		}

	}





}
