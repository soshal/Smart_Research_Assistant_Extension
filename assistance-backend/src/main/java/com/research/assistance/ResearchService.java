package com.research.assistance;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

@Service
public class ResearchService {

    @Value("${gemini.api.url}")
    private  String geminiApiUrl;

    @Value("${gemini.api.key}")
    private  String geminiApiKey;

    private final WebClient webClient;

    private  final ObjectMapper objectMapper;

    public  ResearchService(WebClient.Builder  webBuilder,ObjectMapper objectMappers){
        this.webClient = webBuilder.build();
        this.objectMapper = objectMappers;
    }


    public String processContent(Research research) {
        try {
            String prompt = buildPrompt(research);

            Map<String, Object> request = Map.of(
                    "contents", List.of(
                            Map.of("parts", List.of(Map.of("text", prompt)))
                    )
            );

            // Ensure the URL is correctly formed and the key is added in the query parameter
            String apiUrl = geminiApiUrl + "?key=" + geminiApiKey; // No need to add the key in the URL properties

            System.out.println("Final API URL: " + apiUrl);
            System.out.println("Request Body: " + objectMapper.writeValueAsString(request));

            // ✅ Log the raw API response
            String response = webClient.post()
                    .uri(apiUrl) // Using the constructed URL here
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            System.out.println("Raw API Response: " + response); // Add this to debug response

            return extractText(response);
        } catch (Exception e) {
            System.err.println("API Error: " + e.getMessage());
            return "Error: " + e.getMessage();
        }
    }



    private String extractText(String reponse) {

        try{
            GeminiResponse geminiResponse = objectMapper.readValue(reponse,GeminiResponse.class);
            if(geminiResponse.getCandidates() != null && !geminiResponse.getCandidates().isEmpty()){

                GeminiResponse.Candidate fisrt = geminiResponse.getCandidates().get(0);
                if(fisrt.getContent() != null && fisrt.getContent().getParts() != null && !fisrt.getContent().getParts().isEmpty()){
                    return fisrt.getContent().getParts().get(0).getText();
                }




            }
            return "no content found in response";

        }
        catch (Exception e){
            return  " Error Parsing" + e.getMessage();
        }

    }


    private String buildPrompt(Research research){
        StringBuilder promt = new StringBuilder();

        switch (research.getOperation()) {
            case "summarize":
                promt.append("Provide a clear and consise summary of the followinng content in a few sentences \n");
                break;
            case "suggest":
                promt.append("provide a clear and consise summary of the following text \n");
                break;
            default:
                throw new IllegalArgumentException("Unknown Operation :" + research.getOperation());
        }

        promt.append(research.getContent());

         return  promt.toString();

    }
}
