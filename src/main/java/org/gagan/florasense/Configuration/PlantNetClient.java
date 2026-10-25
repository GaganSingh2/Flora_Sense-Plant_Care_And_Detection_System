package org.gagan.florasense.Configuration;

import org.gagan.florasense.Dto.PlantNetResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import org.springframework.http.*;

@Component
public class PlantNetClient {
    @Autowired
    private RestTemplate restTemplate;

    @Value("${plantnet.api.url}")
    private String apiUrl;

    @Value("${plantnet.api.key}")
    private String apiKey;

    @Value("${plantnet.api.project:all}")
    private String project;

    public PlantNetResponse identify(
            byte[] imageBytes,
            String filename,
            String contentType
    ){
        String url = apiUrl + "/" + project
                + "?api-key={apiKey}";

        //ByteArrayResource represents the image being uploaded.
        ByteArrayResource imageResource = new ByteArrayResource(imageBytes){
            @Override
            public String getFilename() {
                return filename;
            }
        };

        HttpHeaders imageHeaders = new HttpHeaders();

        imageHeaders.setContentType(MediaType.parseMediaType(contentType));

        HttpEntity<ByteArrayResource> imagePart =
                new  HttpEntity<>(imageResource, imageHeaders);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("images", imagePart); //images is the multipart field required by Pl@ntNet.
        body.add("organs", "auto"); //organs=auto lets the service determine which plant part is shown. like organs: leaf, flower, fruit, bark etc.

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        HttpEntity<MultiValueMap<String, Object>> request =
                new HttpEntity<>(body, headers);

        ResponseEntity<PlantNetResponse> response =
                restTemplate.postForEntity( //postForEntity() sends the HTTP POST request and converts the JSON into PlantNetResponse.
                        url,
                        request,
                        PlantNetResponse.class,
                        apiKey
                );

        return response.getBody();

    }
}












