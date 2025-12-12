package org.example.data;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.util.Map;
import java.util.TreeMap;

public class ExchangeRateService {

    private final HttpClient client = HttpClient.newHttpClient();

    /**
     * Получить курс base -> symbol на заданную дату.
     * Если date == null, берём "latest".
     * Если base == symbol, возвращаем 1.0 без запроса.
     */
    public double getRate(String base, String symbol, LocalDate date)
            throws IOException, InterruptedException {

        if (base.equalsIgnoreCase(symbol)) {
            return 1.0;
        }

        String datePart = (date == null) ? "latest" : date.toString(); // YYYY-MM-DD
        String url = "https://api.frankfurter.app/" + datePart +
                "?from=" + base + "&to=" + symbol;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new IOException("HTTP error " + response.statusCode());
        }

        String body = response.body();
        System.out.println("Frankfurter one-day: " + body);

        JsonObject root = JsonParser.parseString(body).getAsJsonObject();
        if (!root.has("rates")) {
            throw new IOException("Ответ без поля rates");
        }

        JsonObject rates = root.getAsJsonObject("rates");
        if (!rates.has(symbol)) {
            throw new IOException("Нет курса для " + symbol);
        }

        return rates.get(symbol).getAsDouble();
    }

    /**
     * История курса base -> symbol на период [start, end].
     */
    public Map<LocalDate, Double> getHistory(
            String base,
            String symbol,
            LocalDate start,
            LocalDate end
    ) throws IOException, InterruptedException {

        Map<LocalDate, Double> map = new TreeMap<>();

        if (base.equalsIgnoreCase(symbol)) {
            for (LocalDate d = start; !d.isAfter(end); d = d.plusDays(1)) {
                map.put(d, 1.0);
            }
            return map;
        }

        String url = "https://api.frankfurter.app/" + start + ".." + end +
                "?from=" + base + "&to=" + symbol;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new IOException("HTTP error " + response.statusCode());
        }

        String body = response.body();
        System.out.println("Frankfurter history: " + body);

        JsonObject root = JsonParser.parseString(body).getAsJsonObject();
        if (!root.has("rates")) {
            throw new IOException("Ответ без поля rates");
        }

        JsonObject rates = root.getAsJsonObject("rates");
        for (var entry : rates.entrySet()) {
            String dateStr = entry.getKey();           // "2024-11-26"
            JsonObject dayObj = entry.getValue().getAsJsonObject();
            if (!dayObj.has(symbol)) continue;
            double rate = dayObj.get(symbol).getAsDouble();
            LocalDate d = LocalDate.parse(dateStr);
            map.put(d, rate);
        }

        if (map.isEmpty()) {
            throw new IOException("Нет данных по истории курса");
        }

        return map;
    }
}
