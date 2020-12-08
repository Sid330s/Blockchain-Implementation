package com.example.Blockchain;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.squareup.okhttp.*;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
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
		blockchain.addNewTransaction(new Transaction("A1","Content 2"));
		boolean ans = blockchain.mine(2);

		SpringApplication.run(BlockchainApplication.class, args);

	}

	@GetMapping("/hello")
	public String hello(@RequestParam(value = "name", defaultValue = "World") String name) {
		return String.format("Hello %s!", name);
	}

	@GetMapping("/chain")
	public String get_chain() {
        ChainStatus chainStatus = new ChainStatus();
        chainStatus.chain = blockchain.getChain();
        chainStatus.length = blockchain.getChain().size();
		chainStatus.peers = peers;
		GsonBuilder builder = new GsonBuilder();
		Gson gson = builder.create();
		String chainStatusDump = gson.toJson(chainStatus);



        return chainStatusDump;
	}

	@PostMapping("/new_transaction")
	public ResponseEntity<String> new_transaction(@org.springframework.web.bind.annotation.RequestBody Transaction transaction){
		System.out.println("Debug:"+transaction.author+" "+transaction.content);
		if(transaction.author.isEmpty())
			return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body("Invalid transaction data");
		blockchain.addNewTransaction(transaction);
		return ResponseEntity.status(HttpStatus.ACCEPTED).body("Success");

	}

	@GetMapping("/mine")
	public String mine_unconfirmed_transactions() {
		boolean result = blockchain.mine(0);
		if (result==false) return "No transactions to mine";

		boolean flag=false;
		try {
			flag = consensus();
		} catch (IOException e) {
			e.printStackTrace();
		}
		if (flag) announce_new_block(blockchain.getLastBlock());

		return String.format("Block %s is mined.", blockchain.getLastBlock().getIndex());
	}



	boolean consensus() throws IOException {

		ArrayList<Block> longest_chain = blockchain.getChain();
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
