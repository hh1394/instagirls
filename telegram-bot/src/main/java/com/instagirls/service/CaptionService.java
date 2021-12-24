package com.instagirls.service;

import lombok.SneakyThrows;
import okhttp3.*;
import org.springframework.stereotype.Service;

@Service
public class CaptionService {

    private final OkHttpClient client = new OkHttpClient();

    @SneakyThrows
    public String getCaption() {


        MediaType mediaType = MediaType.parse("application/json");
        Request request = new Request.Builder()
                .url("https://motivational-quotes1.p.rapidapi.com/motivation")
                .post(RequestBody.create(mediaType, ""))
                .addHeader("content-type", "application/json")
                .addHeader("x-rapidapi-key", "901e0bdc51mshd43424f9a6ac8c6p150a00jsn4ff03c2e3dfa")
                .addHeader("x-rapidapi-host", "motivational-quotes1.p.rapidapi.com")
                .build();

        Response response = client.newCall(request).execute();
        return response.body().string();
    }
}
