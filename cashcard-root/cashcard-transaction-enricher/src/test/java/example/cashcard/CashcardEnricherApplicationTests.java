package example.cashcard;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cloud.stream.binder.test.InputDestination;
import org.springframework.cloud.stream.binder.test.OutputDestination;
import org.springframework.cloud.stream.binder.test.TestChannelBinderConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;

import com.fasterxml.jackson.databind.ObjectMapper;

import example.cashcard.domain.ApprovalStatus;
import example.cashcard.domain.CardHolderData;
import example.cashcard.domain.CashCard;
import example.cashcard.domain.EnrichedTransaction;
import example.cashcard.domain.Transaction;
import example.cashcard.service.EnrichmentService;
import static org.mockito.BDDMockito.given;

@SpringBootTest
@Import(TestChannelBinderConfiguration.class)
class CashcardEnricherApplicationTests {

	@MockBean
	private EnrichmentService enrichmentService;
	
	@Test
	  void enrichmentServiceShouldAddDataToTransactions(@Autowired InputDestination inputDestination,
	                      @Autowired OutputDestination outputDestination) throws IOException {

		Transaction transaction = new Transaction(1L, new CashCard(123L, "sarah1", 1.00));
		EnrichedTransaction enrichedTransaction = new EnrichedTransaction(
			    transaction.id(),
			    transaction.cashCard(),
			    ApprovalStatus.APPROVED,
			    new CardHolderData(UUID.randomUUID(), transaction.cashCard().owner(), "123 Main Street"));

		given(enrichmentService.enrichTransaction(transaction)).willReturn(enrichedTransaction);

	    Message<Transaction> message = MessageBuilder.withPayload(transaction).build();
	    inputDestination.send(message, "enrichTransaction-in-0");  

	    /*
	     * InputDestination is enabled after importing TestChannelBinderConfiguration.
	     * InputDestination/OutputDestination are test utility related to messaging server.
	     * InputDestination is like a input queue where all incoming message to broker/server queue up with specified topic name.
	     * inputDestiantion.send() method is to send the content into aforementioned queue. */
	    
	    Message<byte[]> result = outputDestination.receive(5000, "enrichTransaction-out-0");
	    assertThat(result).isNotNull();
	    
	    ObjectMapper objectMapper = new ObjectMapper();
	    EnrichedTransaction receivedData = objectMapper.readValue(result.getPayload(), EnrichedTransaction.class);

	    assertThat(receivedData).isEqualTo(enrichedTransaction);

	  }

	  @SpringBootApplication
	  public static class App {

	  }

}
