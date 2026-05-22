package com.ta.util;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * Client for invoking large-language-model APIs configured via {@link ConfigDAO}.
 * <p>
 * Supports Anthropic ({@code claude-*}) and OpenAI-compatible endpoints. Provider, model,
 * API key, and base URL are read from configuration keys {@code ai.provider},
 * {@code ai.model}, {@code ai.api.key}, and {@code ai.base.url} respectively.
 * Defaults to Anthropic with {@code claude-haiku-4-5-20251001} when not configured.
 * </p>
 */
public class AIService {

    /**
     * Sends a user prompt to the configured AI provider and returns the model's text response.
     * <p>
     * When {@code ai.provider} is {@code "anthropic"} (the default), the Anthropic Messages API
     * is used. For any other provider value, an OpenAI-compatible chat completions endpoint
     * is called using {@code ai.base.url} (default {@code https://api.openai.com/v1}).
     * </p>
     *
     * @param config the configuration accessor supplying API credentials and provider settings
     * @param prompt the user message to send to the model
     * @return the assistant's text response extracted from the API JSON payload
     * @throws IOException          if the HTTP request fails or the API returns a non-200 status
     * @throws InterruptedException if the HTTP client is interrupted while waiting for a response
     */
    public static String call(ConfigDAO config, String prompt) throws IOException, InterruptedException {
        String provider = config.get("ai.provider");
        String apiKey   = config.get("ai.api.key");
        String model    = config.get("ai.model");

        if (provider == null || provider.trim().isEmpty()) provider = "anthropic";
        if (model    == null || model.trim().isEmpty())
            model = "anthropic".equals(provider) ? "claude-haiku-4-5-20251001" : "gpt-4o-mini";

        if ("anthropic".equals(provider)) {
            return callAnthropic(apiKey, model, prompt);
        } else {
            String baseUrl = config.get("ai.base.url");
            if (baseUrl == null || baseUrl.trim().isEmpty()) baseUrl = "https://api.openai.com/v1";
            return callOpenAICompat(apiKey, baseUrl.trim(), model, prompt);
        }
    }

    private static String callAnthropic(String apiKey, String model, String prompt)
            throws IOException, InterruptedException {
        String body = "{\"model\":" + jsonStr(model) + ",\"max_tokens\":1024,"
                + "\"messages\":[{\"role\":\"user\",\"content\":" + jsonStr(prompt) + "}]}";

        HttpResponse<String> resp = send(
                "https://api.anthropic.com/v1/messages",
                body,
                new String[]{"x-api-key", apiKey, "anthropic-version", "2023-06-01", "content-type", "application/json"}
        );

        if (resp.statusCode() != 200)
            throw new IOException("Anthropic API " + resp.statusCode() + ": " + resp.body());

        JsonObject root = JsonParser.parseString(resp.body()).getAsJsonObject();
        return root.getAsJsonArray("content").get(0).getAsJsonObject().get("text").getAsString();
    }

    private static String callOpenAICompat(String apiKey, String baseUrl, String model, String prompt)
            throws IOException, InterruptedException {
        String url = baseUrl.replaceAll("/$", "") + "/chat/completions";
        String body = "{\"model\":" + jsonStr(model) + ",\"temperature\":0.3,"
                + "\"messages\":[{\"role\":\"user\",\"content\":" + jsonStr(prompt) + "}]}";

        HttpResponse<String> resp = send(
                url, body,
                new String[]{"Authorization", "Bearer " + apiKey, "content-type", "application/json"}
        );

        if (resp.statusCode() != 200)
            throw new IOException("API " + resp.statusCode() + ": " + resp.body());

        JsonObject root = JsonParser.parseString(resp.body()).getAsJsonObject();
        return root.getAsJsonArray("choices").get(0).getAsJsonObject()
                .getAsJsonObject("message").get("content").getAsString();
    }

    private static HttpResponse<String> send(String url, String body, String[] headers)
            throws IOException, InterruptedException {
        HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(15)).build();
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(30))
                .POST(HttpRequest.BodyPublishers.ofString(body));
        for (int i = 0; i + 1 < headers.length; i += 2) {
            builder.header(headers[i], headers[i + 1]);
        }
        return client.send(builder.build(), HttpResponse.BodyHandlers.ofString());
    }

    private static String jsonStr(String s) {
        if (s == null) return "null";
        return "\"" + s.replace("\\", "\\\\").replace("\"", "\\\"")
                .replace("\n", "\\n").replace("\r", "\\r").replace("\t", "\\t") + "\"";
    }
}
