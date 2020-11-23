package com.example.Blockchain;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;

@SpringBootApplication
@RestController
public class BlockchainApplication {
	private static Blockchain blockchain ;

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
	public ArrayList<Block> get_chain() {

		return blockchain.getChain();
	}


}
