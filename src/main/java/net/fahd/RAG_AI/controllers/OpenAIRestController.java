package net.fahd.RAG_AI.controllers;

import net.fahd.RAG_AI.service.RagService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OpenAIRestController {

    private ChatClient chatClient;

    @Autowired
    private RagService ragService;

    public OpenAIRestController(ChatClient.Builder chatClient) {
        this.chatClient = chatClient
                .build();
    }

    @GetMapping("/chat")
    public String chat(String query){
        String systemMessage = """
                Vous etes un assistant qui travaille pour un editeur de logiciel
                Vous serez demandé de faire une synthése au format json de la question posée par l'utilisateur
                """;
        return  chatClient.prompt()
                .system(systemMessage)
                .user(query)
                .call().content();
    }

    @GetMapping("/ragtest")
    public  String rag(String query){
        String response = ragService.askLLM(query);
        return response;
    }


}
