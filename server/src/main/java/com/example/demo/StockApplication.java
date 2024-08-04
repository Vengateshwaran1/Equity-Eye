package com.example.demo;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.stream.Collectors;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.CrossOrigin;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootApplication
@RestController
@RequestMapping("/api/stock")
@CrossOrigin(origins = "*")
public class StockApplication {

  private static final String API_KEY = "YOUR_ALPHA_VANTAGE_API_KEY"; // Replace with your Alpha Vantage API key

  @SuppressWarnings("unchecked")
  @GetMapping("/{symbol}")
  public ResponseEntity<Map<String, Object>> getStockData(@PathVariable String symbol) throws IOException {
    String url = String.format("https://www.alphavantage.co/query?function=TIME_SERIES_DAILY&symbol=%s&outputsize=compact&apikey=%s", symbol, API_KEY);

    // Fetch data from Alpha Vantage API using HttpURLConnection
    String response = fetchUrl(url);

    // Parse JSON response
    Map<String, Object> data = new HashMap<>();
    try {
      ObjectMapper mapper = new ObjectMapper();
      data = mapper.readValue(response, Map.class);
    } catch (JsonParseException | JsonMappingException e) {
      e.printStackTrace();
      return ResponseEntity.badRequest().body(Map.of("error", "Failed to parse response"));
    }

    // Extract relevant data
    Map<String, Object> timeSeriesDaily = (Map<String, Object>) data.get("Time Series (Daily)");
    if (timeSeriesDaily == null) {
      return ResponseEntity.badRequest().body(Map.of("error", "Invalid response format"));
    }

    // Prepare data (can be modified to suit your needs)
    Map<String, Object> formattedData = new HashMap<>();
    formattedData.put("timeSeriesDaily", timeSeriesDaily.entrySet().stream()
        .map(entry -> {
          Map<String, Object> dailyData = new HashMap<>();
          dailyData.put("date", entry.getKey());
          dailyData.put("open", ((Map<String, Object>) entry.getValue()).get("1. open"));
          return dailyData;
        })
        .collect(Collectors.toList()));

    return ResponseEntity.ok(formattedData);
  }

  private String fetchUrl(String urlString) throws IOException {
    URL url = new URL(urlString);
    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
    connection.setRequestMethod("GET");
    connection.connect();

    int responseCode = connection.getResponseCode();
    if (responseCode != 200) {
      throw new IOException("Failed to fetch data: HTTP status code " + responseCode);
    }

    Scanner scanner = new Scanner(connection.getInputStream());
    scanner.useDelimiter("\\A");
    String response = scanner.hasNext() ? scanner.next() : "";
    scanner.close();
    connection.disconnect();

    return response;
  }
}
