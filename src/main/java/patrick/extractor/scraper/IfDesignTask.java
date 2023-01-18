package patrick.extractor.scraper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CountDownLatch;
import org.springframework.http.HttpStatus;
import static patrick.extractor.scraper.Scraper.apiEndpoint;
import static patrick.extractor.scraper.Scraper.totalItemsScanned;
import patrick.extractor.scraper.response.CompanyResponse;

public class IfDesignTask implements Runnable {

    private final int page;
    private final ObjectMapper objectMapper;
    private final HttpClient client;
    private final String requestBody;
    private final IfDesignFileWriterTask ifDesignFileWriterTask;

    public IfDesignTask(
            int page,
            ObjectMapper objectMapper,
            HttpClient client,
            String requestBody,
            IfDesignFileWriterTask ifDesignFileWriterTask
    ) {
        this.page = page;
        this.objectMapper = objectMapper;
        this.client = client;
        this.requestBody = requestBody;
        this.ifDesignFileWriterTask = ifDesignFileWriterTask;
    }

    @Override
    public void run() {
        HttpRequest httpRequest;
        try {
            httpRequest = HttpRequest.newBuilder(new URI(String.format("%s/company/all/%s", apiEndpoint, page)))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();
            HttpResponse<String> res = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());
            if (res.statusCode() == HttpStatus.OK.value()) {
                try {
                    CompanyResponse response = objectMapper.readValue(res.body(), CompanyResponse.class);
                    ifDesignFileWriterTask.getQueue().put(response);
                    totalItemsScanned += response.items.size();
                    System.out.println(String.format("Page: %s - Items in page: %s - Item Scanned: %s", page, response.items.size(), totalItemsScanned));
                    /*
                        response.count is misleading and it is not tied to the current items in the collection.
                        Also sometime response returns an empty array of items and count equals to 0. Keep going and don't stop the writer task
                        At worse by timeout
                     */
                    if (totalItemsScanned >= response.count && response.count != 0) {
                        System.out.println(String.format("Stopping by condition. Total item scanned: %s - response: %s", totalItemsScanned, objectMapper.writeValueAsString(response)));
                        ifDesignFileWriterTask.stop();
                    }
                } catch (JsonProcessingException | InterruptedException ex) {
                    throw new UnsupportedOperationException(ex.getMessage());
                }
            }
        } catch (URISyntaxException | IOException | InterruptedException ex) {
            throw new UnsupportedOperationException(ex.getMessage());
        }

    }

}
