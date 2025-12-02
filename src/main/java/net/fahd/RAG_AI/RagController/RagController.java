package net.fahd.RAG_AI.RagController;

import net.fahd.RAG_AI.service.RagService;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Controller
public class RagController {
    private RagService ragService;

    public RagController(RagService ragService) {
        this.ragService = ragService;
    }

    @GetMapping("/rag")
    public String index(@RequestParam(name = "query" , defaultValue = "") String query , Model model){
        if(query.equals("")) return "rag";

        String response = ragService.askLLM(query);
        model.addAttribute("query",query);
        model.addAttribute("response" , response);
        return "rag";
    }

    @PostMapping("/uploadPdfMultiple")
    @ResponseBody
    public String uploadPdfMultiple(@RequestParam("files") MultipartFile[] files) {
        try {
            List<Resource> pdfList = new ArrayList<>();

            for (MultipartFile file : files) {
                pdfList.add(new ByteArrayResource(file.getBytes()) {
                    @Override
                    public String getFilename() {
                        return file.getOriginalFilename();
                    }
                });
            }

            ragService.processMultiplePdfs(pdfList);

            return "OK";

        } catch (Exception e) {
            return "ERROR: " + e.getMessage();
        }
    }

}
