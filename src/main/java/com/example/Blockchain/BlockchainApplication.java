package com.example.Blockchain;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.squareup.okhttp.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Configuration
@SpringBootApplication
@RestController
public class BlockchainApplication {

	public static String serverAddress;
	public static int serverPort;
	public String getHostUrl() {
		return "http://" + BlockchainApplication.serverAddress + ":" + BlockchainApplication.serverPort+"/";
	}

	private static Blockchain blockchain ;
    public static Set<String> peers = new HashSet<>();




	public static void main(String[] args) {

		blockchain = new Blockchain();
		System.out.println("Hello");
		blockchain.createGenesisBlock();
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
		chainStatus.peers = new ArrayList<String>(this.peers);
		GsonBuilder builder = new GsonBuilder();
		Gson gson = builder.create();
		String chainStatusDump = gson.toJson(chainStatus);



        return chainStatusDump;
	}

	@PostMapping("/add_block")
	public ResponseEntity<String> verify_and_add_block(@org.springframework.web.bind.annotation.RequestBody String blockdump1){
		System.out.println(blockdump1);
		GsonBuilder builder = new GsonBuilder();
		Gson gson = builder.create();
		Block block1 = gson.fromJson(blockdump1, Block.class);
		System.out.println(gson.toJson(block1));
		System.out.println("Block.hash:"+block1.getHash());
		boolean isAdded = blockchain.addBlock(block1,block1.getHash());
		if(isAdded) System.out.println("is Added True");
		else System.out.println("is added False");
		return ResponseEntity.status(HttpStatus.ACCEPTED).body("Success");

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
		if (flag) {
			try {
				System.out.println("Trying to Announce");
				announce_new_block(blockchain.getLastBlock());
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return String.format("Block %s is mined.", blockchain.getLastBlock().getIndex());
	}

	@PostMapping("/register_node")
	public String register_new_peers(@org.springframework.web.bind.annotation.RequestBody  NodeObject nodeObject){

		if (nodeObject.nodeAddress.isEmpty())
			return "Empty Address";

		System.out.println("I Got that Peer Address: "+nodeObject.nodeAddress);
		this.peers.add(nodeObject.nodeAddress);
		System.out.println("I Added that Address:"+peers.size());
		for(String peer:this.peers){
			System.out.println("-->"+peer);
		}
		return get_chain();
	}

	@PostMapping("/register_with")
	public  ResponseEntity<String> register_with_existing_node(@org.springframework.web.bind.annotation.RequestBody  NodeObject nodeObject, @Autowired HttpServletRequest httpRequest) throws IOException {

		BlockchainApplication.serverAddress = httpRequest.getServerName();
		BlockchainApplication.serverPort = httpRequest.getServerPort();
		String parentAddress=nodeObject.nodeAddress;
		System.out.println("Parent NodeAddress:"+parentAddress);
		if (nodeObject.nodeAddress.isEmpty())
			ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body("Invalid transaction data");

		OkHttpClient client = new OkHttpClient();
		GsonBuilder builder = new GsonBuilder();
		builder.setPrettyPrinting();
		Gson gson = builder.create();

		System.out.println(getHostUrl());
		nodeObject.setNodeAddress(getHostUrl());

		String requestString = gson.toJson(nodeObject);
		RequestBody requestBody = RequestBody.create(
				MediaType.parse("application/json"), requestString);

		System.out.println("Request Body Created");

		Request request = new Request.Builder()
				.url(parentAddress+"/register_node")
				.addHeader("Content-Type", "application/json")
				.post(requestBody)
				.build();

		System.out.println("Request Sent");
		Response response = client.newCall(request).execute();
		String chainStatusDump = response.body().string();
		System.out.println("Response Got");
		ChainStatus chainStatus = gson.fromJson(chainStatusDump, ChainStatus.class);
		System.out.println("Chain Got");
		blockchain.setChain(chainStatus.chain);
		Set<String> temp = new HashSet<>(chainStatus.peers);
		this.peers=temp;
		System.out.println("Chain Synced");
		for(String peer:this.peers){
			System.out.println("-->"+peer);
		}
		return ResponseEntity.status(HttpStatus.ACCEPTED).body("Success");
	}

	boolean consensus() throws IOException {

		ArrayList<Block> longest_chain = blockchain.getChain();
		int current_len = blockchain.getChain().size();


		for (String node : peers){

			OkHttpClient client = new OkHttpClient();
			Request request = new Request.Builder()
					.url(node + "/chain")
					.build();
			Response response = client.newCall(request).execute();
			String chainStatusDump = response.body().string();
			System.out.println("Response Got");
			GsonBuilder builder = new GsonBuilder();
			builder.setPrettyPrinting();
			Gson gson = builder.create();
			ChainStatus chainStatus = gson.fromJson(chainStatusDump, ChainStatus.class);
			System.out.println("Chain Got");
			blockchain.setChain(chainStatus.chain);
			int length = chainStatus.length;
			ArrayList<Block>chain = chainStatus.chain;
			if (length > current_len && blockchain.checkChainValidity(chain)){
				current_len = length;
				longest_chain = chain;
			}

		}
//mvnw spring-boot:run -Dspring-boot.run.arguments=--server.port=8010


		if (longest_chain.size()>0){
			blockchain.setChain(longest_chain);
			return true;
		}


		return false;
	}



	void announce_new_block(Block block) throws IOException {
		for (String peer : peers){
			System.out.println("announcing to :"+peer);
			String url = peer+"/add_block";

			OkHttpClient client = new OkHttpClient();
			GsonBuilder builder = new GsonBuilder();
			builder.setPrettyPrinting();
			Gson gson = builder.create();
			String requestString = gson.toJson(block);
			System.out.println(requestString);
			RequestBody requestBody = RequestBody.create(
					MediaType.parse("application/json"), requestString);

			Request request = new Request.Builder()
					.url(url)
					.addHeader("Content-Type", "application/json")
					.post(requestBody)
					.build();

			Response response = client.newCall(request).execute();

			System.out.println("announed to :"+peer);

		}

	}

}
