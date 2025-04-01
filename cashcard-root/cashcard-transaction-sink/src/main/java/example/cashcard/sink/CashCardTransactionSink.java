package example.cashcard.sink;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.StringJoiner;
import java.util.function.Consumer;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import example.cashcard.domain.EnrichedTransaction;

@Configuration
public class CashCardTransactionSink {
	
	public static final String CSV_FILE_PATH = System.getProperty("user.dir") + "/transactions-output.csv";
	
	@Bean
	public Consumer<EnrichedTransaction> sinkToConsole(){
		return tx -> {
			System.out.println("Transaction received: " + tx);
		};
	}
	
	@Bean
    public Consumer<EnrichedTransaction> cashCardTransactionFileSink() {
        return enrichedTransaction -> {
            StringJoiner joiner = new StringJoiner(",");
            StringJoiner enrichedTxnTextual = joiner.add(String.valueOf(enrichedTransaction.id()))
                    .add(String.valueOf(enrichedTransaction.cashCard().id()))
                    .add(String.valueOf(enrichedTransaction.cashCard().amountRequestedForAuth()))
                    .add(enrichedTransaction.cardHolderData().name())
                    .add(enrichedTransaction.cardHolderData().userId().toString())
                    .add(enrichedTransaction.cardHolderData().address())
                    .add(enrichedTransaction.approvalStatus().name());
            Path path = Paths.get(CSV_FILE_PATH);
            try {
                ensureSinkFileExists();
                Files.writeString(path, enrichedTxnTextual.toString() + "\n", StandardOpenOption.APPEND);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        };
    }

    // Also add this helper method
    private void ensureSinkFileExists() throws IOException {
        new File(CSV_FILE_PATH).createNewFile();
    }
}


