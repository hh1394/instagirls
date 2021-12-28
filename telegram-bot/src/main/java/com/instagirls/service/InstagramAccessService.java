package com.instagirls.service;

import com.instagirls.dto.InstagramPostDTO;
import com.instagirls.exception.*;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.ExecutionException;

@Service
public class InstagramAccessService {

    final static private String INSTAGRAM_SERVICE_BASE_URL = System.getenv("ig_service_url");
    final static private HttpClient HTTP_CLIENT = HttpClient.newBuilder().build();

    public InstagramPostDTO getNewMostLikedPostFromAccount(final String instagramUsername) {
        final URI uri = buildURI(INSTAGRAM_SERVICE_BASE_URL + "/posts/accounts/" + instagramUsername);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .GET()
                .build();

        return sendRequestForDTO(request);
    }

    public InstagramPostDTO getNewMostLikedPostFromRandomAccount() {

        final URI uri = buildURI(INSTAGRAM_SERVICE_BASE_URL + "/posts/random/");

        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .GET()
                .build();

        return sendRequestForDTO(request);

    }

    public void setPosted(final String postCode) {

        final URI uri = buildURI(INSTAGRAM_SERVICE_BASE_URL + "/posts/" + postCode);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .method("PATCH", HttpRequest.BodyPublishers.noBody())
                .build();

        sendRequest(request);
    }

    public void loadNewAccount(final String accountUsername) throws InstagramAccountDoesntExistException {
        final URI uri = buildURI(INSTAGRAM_SERVICE_BASE_URL + "/accounts/" + accountUsername);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .POST(HttpRequest.BodyPublishers.noBody())
                .build();

        sendRequest(request);
    }

    public void disableAccount(final String accountUsername) {
        final URI uri = buildURI(INSTAGRAM_SERVICE_BASE_URL + "/accounts/" + accountUsername);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .DELETE()
                .build();

        sendRequest(request);
    }

    // TODO parametrize?
    private InstagramPostDTO sendRequestForDTO(final HttpRequest request) {
        try {
            return HTTP_CLIENT.sendAsync(request, new JsonBodyHandler<>(InstagramPostDTO.class)).get().body().get();
        } catch (InterruptedException e) {
            throw new UncheckedInterruptedException(e);
        } catch (ExecutionException e) {
            throw new UncheckedExecutionException(e);
        }
    }

    private void sendRequest(final HttpRequest request) {
        try {
            int statusCode = HTTP_CLIENT.sendAsync(request, HttpResponse.BodyHandlers.discarding()).get().statusCode();
            if (statusCode != 200) {
                throw new InternalRequestFailedException(statusCode);
            }
        } catch (InterruptedException e) {
            throw new UncheckedInterruptedException(e);
        } catch (ExecutionException e) {
            throw new UncheckedExecutionException(e);
        }
    }

    @NotNull
    private URI buildURI(final String uri) {
        try {
            return new URI(uri);
        } catch (URISyntaxException e) {
            throw new URIException(e);
        }
    }
}
