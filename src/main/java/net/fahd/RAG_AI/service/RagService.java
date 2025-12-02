package net.fahd.RAG_AI.service;

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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class RagService {

    private VectorStore vectorStore;
    private ChatClient chatClient;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Value("classpath:pdfs/*")
    private Resource[] pdfResources;

    public RagService(VectorStore vectorStore, ChatClient.Builder chatClient) {
        this.vectorStore = vectorStore;
        this.chatClient = chatClient.build();
    }

    public String askLLM( String query) {

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

       /* ChatClient client = chatClient;
        String result = client.prompt(prompt).call().content();
        System.out.println("\n===== RÉPONSE LLM =====");
        System.out.println(result);*/

        return chatClient.prompt(prompt).call().content();

    }

    /**
     * Embed all PDFs inside the vector store
     */
    public void textEmbedding(

            Resource [] pdfResources
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

    public void processMultiplePdfs(List<Resource> pdfFiles) {
        try {
            jdbcTemplate.update("DELETE FROM vector_store");

            PdfDocumentReaderConfig config = PdfDocumentReaderConfig.defaultConfig();
            TokenTextSplitter tokenTextSplitter =
                    new TokenTextSplitter(800, 350, 5, 10000, true);

            List<Document> allChunks = new ArrayList<>();

            for (Resource pdf : pdfFiles) {

                System.out.println("📄 Traitement : " + pdf.getFilename());

                PagePdfDocumentReader reader = new PagePdfDocumentReader(pdf, config);

                List<Document> pdfDocs = reader.get();
                List<Document> chunks = tokenTextSplitter.apply(pdfDocs);

                allChunks.addAll(chunks);

                System.out.println(" ➤ Chunks pour " + pdf.getFilename() + " : " + chunks.size());
            }

            vectorStore.accept(allChunks);

            System.out.println("📌 Total chunks insérés : " + allChunks.size());

        } catch (Exception e) {
            throw new RuntimeException("Erreur traitement PDFs", e);
        }
    }

}
