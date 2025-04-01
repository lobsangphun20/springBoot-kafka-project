package example.cashcard;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.List;
import java.util.UUID;

import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.cloud.stream.binder.test.InputDestination;
import org.springframework.cloud.stream.binder.test.TestChannelBinderConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;

import example.cashcard.domain.ApprovalStatus;
import example.cashcard.domain.CardHolderData;
import example.cashcard.domain.CashCard;
import example.cashcard.domain.EnrichedTransaction;
import example.cashcard.domain.Transaction;
import example.cashcard.sink.CashCardTransactionSink;

@SpringBootTest(properties = "spring.cloud.function.definition=sinkToConsole;cashCardTransactionFileSink")
@Import(TestChannelBinderConfiguration.class)
@ExtendWith(OutputCaptureExtension.class)
class CashcardTransactionSinkApplicationTests {

	private static final int AWAIT_DURATION = 10;
	
	@Test
	void cashCardSinkToConsole(@Autowired InputDestination inputDestination, CapturedOutput output)  {

	    // Set up the expected data
	    Transaction transaction = new Transaction(1L, new CashCard(123L, "Kumar Patel", 1.00));
	    EnrichedTransaction enrichedTransaction = new EnrichedTransaction(
	      transaction.id(),
	      transaction.cashCard(),
	      ApprovalStatus.APPROVED,
	      new CardHolderData(UUID.randomUUID(), transaction.cashCard().owner(), "123 Main Street"));

	    // Send the message to the console sink's input topic
	    Message<EnrichedTransaction> message = MessageBuilder.withPayload(enrichedTransaction).build();
	    inputDestination.send(message, "sinkToConsole-in-0");
	    
	 // Wait for, then test the console output
	    Awaitility.await().atMost(Duration.ofSeconds(AWAIT_DURATION))
	            .until(() -> output.toString().contains("Transaction received"));
	}
	
	@Test
	void sinkToFile(@Autowired InputDestination inputDestination) throws IOException {

	    // Setup the expected data
	    Transaction transaction = new Transaction(1L, new CashCard(123L, "Kumar Patel", 100.25));
	    UUID uuid = UUID.fromString("65d0b699-3695-44c6-ba23-4a241717dab7");
	    EnrichedTransaction enrichedTransaction = new EnrichedTransaction(
	      transaction.id(),
	      transaction.cashCard(),
	      ApprovalStatus.APPROVED,
	      new CardHolderData(uuid, transaction.cashCard().owner(), "123 Main Street"));

	    // Send the message to the sink's input
	    Message<EnrichedTransaction> message = MessageBuilder.withPayload(enrichedTransaction).build();
	    inputDestination.send(message, "cashCardTransactionFileSink-in-0");

	    // Wait for the sink's output file to be written
	    Path path = Paths.get(CashCardTransactionSink.CSV_FILE_PATH);
	    Awaitility.await().until(() -> Files.exists(path));

	    // Read from the output file and make sure the content is correct
	    List<String> lines = Files.readAllLines(path);
	    assertThat(lines.get(0)).isEqualTo
	            ("1,123,100.25,Kumar Patel,65d0b699-3695-44c6-ba23-4a241717dab7,123 Main Street,APPROVED");
	}

}
