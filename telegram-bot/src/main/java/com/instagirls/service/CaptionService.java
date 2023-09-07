package com.instagirls.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.instagirls.dto.InstagramPostDTO;
import com.instagirls.exception.URIException;
import lombok.SneakyThrows;
import okhttp3.*;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.util.ArrayList;
import java.util.List;

@Service
public class CaptionService {

    private final OkHttpClient client = new OkHttpClient();

    @SneakyThrows
    public String getCaption() {

        MediaType mediaType = MediaType.parse("application/json");
        Request request = new Request.Builder()
                .url("https://andruxnet-random-famous-quotes.p.rapidapi.com/")
                .post(RequestBody.create(mediaType, ""))
                .addHeader("content-type", "application/json")
                .addHeader("X-RapidAPI-Key", "901e0bdc51mshd43424f9a6ac8c6p150a00jsn4ff03c2e3dfa")
                .addHeader("X-RapidAPI-Host", "andruxnet-random-famous-quotes.p.rapidapi.com")
                .get()
                .build();

        try (Response response = client.newCall(request).execute()) {
            String responseAsString = response.body().string();
            ObjectMapper mapper = new ObjectMapper();
            Caption[] captions = mapper.readValue(responseAsString, Caption[].class);
            return captions[0].buildCaption();
        }
    }

}
