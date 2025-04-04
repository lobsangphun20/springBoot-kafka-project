package example.cashcard.controller;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.cloud.stream.binder.test.OutputDestination;
import org.springframework.cloud.stream.binder.test.TestChannelBinderConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.Message;

import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.ObjectMapper;

import example.cashcard.domain.CashCard;
import example.cashcard.domain.Transaction;
import example.cashcard.ondemand.CashCardTransactionOnDemand;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@Import({TestChannelBinderConfiguration.class, CashCardTransactionOnDemand.class})   // Basically imports bean definition from another configuration classes. Here we are importing bean called OutputDesination
public class CashCardControllerTests {

	@Autowired
	TestRestTemplate restTemplate;
	
	@LocalServerPort
	private Long port;
	
	@Test
	void postShouldSendTransactionAsAMessage(@Autowired OutputDestination outputDestination) throws StreamReadException, DatabindException, IOException {
		
		Transaction postedTransaction = new Transaction(123L, new CashCard(1L, "Foo Bar", 1.00));
	    
		ResponseEntity<Transaction> response = restTemplate.postForEntity("http://localhost:" + port +"/publish/txn", postedTransaction, Transaction.class);
		
		Message<byte[]> result = outputDestination.receive(5000,"approvalRequest-out-0"); //outputDestination seems to acts like a broker
		
		
		assertThat(result).isNotNull();
	    
		ObjectMapper objectMapper = new ObjectMapper();
	    Transaction transactionFromMessage = objectMapper.readValue(result.getPayload(), Transaction.class);
	    
	    assertThat(transactionFromMessage.id()).isEqualTo(postedTransaction.id());
	    
		
	}
	  @SpringBootApplication
	  public static class App {

	  }
}
