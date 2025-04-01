package example.cashcard.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import example.cashcard.domain.Transaction;
import example.cashcard.ondemand.CashCardTransactionOnDemand;
import example.cashcard.stream.CashCardTransactionStream;

@RestController
public class CashCardController {
	
	private CashCardTransactionOnDemand cardOnDemand;
	
	

	public CashCardController(CashCardTransactionOnDemand cardOnDemand) {
		this.cardOnDemand = cardOnDemand;
	}


	@PostMapping(path  = "/publish/txn")
	public void publishTxn(@RequestBody Transaction transaction) {
		System.out.println("POST for Transaction: " + transaction);
		this.cardOnDemand.publishOnDemand(transaction);
		
	}
}
