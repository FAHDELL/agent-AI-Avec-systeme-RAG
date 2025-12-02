package net.fahd.RAG_AI;

import net.fahd.RAG_AI.service.RagService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.reader.pdf.config.PdfDocumentReaderConfig;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@SpringBootApplication
public class RagAiApplication {

	public static void main(String[] args) {
		// SOLUTION ICI : Active l'ancien tri pour éviter l'erreur PDFBox/TimSort
		System.setProperty("java.util.Arrays.useLegacyMergeSort", "true");
		SpringApplication.run(RagAiApplication.class, args);
	}

	@Bean
	CommandLineRunner commandLineRunner(RagService ragService,
										@Value("classpath:pdfs/*") Resource [] pdfResources) {
		return args -> {
			//textEmbedding(vectorStore, jdbcTemplate ,pdfResources );

			String query = """
				donne moi au format json pour chaque thése : le titre de la these , l'auteur , le directeur , et le membres de jury (nom , grade , affiliation , role)
				""";

			//askLLM(vectorStore, chatClient , query );

			/*System.out.println("depart textembidding");
			ragService.textEmbedding(pdfResources);
			System.out.println("fin textembidding");
			ragService.askLLM(query);*/
		};
	}

	private static void askLLM(VectorStore vectorStore, ChatClient.Builder chatClient , String query) {

		List<Document> documentList = vectorStore.similaritySearch(query);
		String systemPromptTemplate = """
				Answer the following question based only on the provided CONTEXT
				If the answer in not found in the context , respond "I don't know".
				CONTEXT :
				{CONTEXT}
				""";
		Message systemMessage = new SystemPromptTemplate(systemPromptTemplate).createMessage(Map.of("CONTEXT", documentList));
		UserMessage userMessage = new UserMessage(query);
		Prompt prompt = new Prompt(List.of(systemMessage,userMessage));

		ChatClient client = chatClient.build();
		String result = client.prompt(prompt).call().content();
		System.out.println("\n===== RÉPONSE LLM =====");
		System.out.println(result);
	}

	/**
	 * Embed all PDFs inside the vector store
	 */
	private static void textEmbedding(
			VectorStore vectorStore,
			JdbcTemplate jdbcTemplate,
			Resource[] pdfResources
	) {
		try {
			jdbcTemplate.update("DELETE FROM vector_store");

			PdfDocumentReaderConfig config = PdfDocumentReaderConfig.defaultConfig();
			TokenTextSplitter tokenTextSplitter = new TokenTextSplitter(
					800, 350, 5, 10000, true
			);

			List<Document> allChunks = new ArrayList<>();

			for (Resource pdf : pdfResources) {
				System.out.println("📄 Traitement du fichier: " + pdf.getFilename());

				PagePdfDocumentReader reader = new PagePdfDocumentReader(pdf, config);

				// Lire PDF
				List<Document> pdfDocs = reader.get();

				// Split en chunks
				List<Document> chunks = tokenTextSplitter.apply(pdfDocs);

				System.out.println(" ➤ Chunks créés pour " + pdf.getFilename() + " : " + chunks.size());

				allChunks.addAll(chunks);
			}

			// Stocker tous les chunks
			System.out.println("****************************************");
			System.out.println("pret a db ");
			System.out.println("****************************************");
			System.out.println(allChunks);
			System.out.println();
			vectorStore.accept(allChunks);

			System.out.println("📌 Total chunks insérés: " + allChunks.size());

		} catch (Exception e) {
			throw new RuntimeException(e);
		}

	}

}
