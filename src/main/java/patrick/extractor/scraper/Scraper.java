package patrick.extractor.scraper;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import patrick.extractor.scraper.request.ApiRequest;
import patrick.extractor.scraper.response.CompanyResponse;

public class Scraper {

    protected static final String apiEndpoint = "https://if-website-search.azurewebsites.net/api";
    protected static final String fileName = "ifdesign.csv";
    protected static volatile int totalItemsScanned = 0;
    protected static final int WRITER_TIMEOUT = 180;

    public static void main(String[] args) throws IOException, InterruptedException, URISyntaxException, ExecutionException {

        Path path = Paths.get(fileName);
        try {
            Files.deleteIfExists(path);
        } catch (IOException e) {
            System.out.println("File does not exist, continuing");
        }

        HttpClient client = HttpClient.newBuilder()
                .followRedirects(java.net.http.HttpClient.Redirect.ALWAYS)
                .build();

        ObjectMapper objectMapper = new Jackson2ObjectMapperBuilder()
                .failOnEmptyBeans(false)
                .modules(new ParameterNamesModule(), new JavaTimeModule())
                .autoDetectFields(true)
                .featuresToDisable(JsonGenerator.Feature.FLUSH_PASSED_TO_STREAM)
                .featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .featuresToDisable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .featuresToDisable(DeserializationFeature.READ_DATE_TIMESTAMPS_AS_NANOSECONDS)
                .build();

        final CsvMapper csvMapper = new CsvMapper();
        final CsvSchema schema = csvMapper.schemaFor(CompanyResponse.CompanyItem.class).withColumnSeparator('\t');

        final ObjectWriter csvWriter = csvMapper.writer(schema);
        

        String requestBody = objectMapper
                .writerWithDefaultPrettyPrinter()
                .writeValueAsString(ApiRequest.of(2));
                //.writeValueAsString(ApiRequest.of(1, 2)); //company and design studios

        HttpRequest httpRequest = HttpRequest.newBuilder(new URI(String.format("%s/company/all/0", apiEndpoint)))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> res = client.send(httpRequest, HttpResponse.BodyHandlers.ofString());
        if (res.statusCode() == HttpStatus.OK.value()) {
            CompanyResponse response = objectMapper.readValue(res.body(), CompanyResponse.class);
            /*
                Attention: response.count DOES NOT equals total amount of items in collection...
                I can not stop the future by comparing item scanned with count and stop the writer future when all items have been scanned. Using timeout at worst or page at best
             */
            BlockingQueue<CompanyResponse> queue = new ArrayBlockingQueue<>(response.count);
            IfDesignFileWriterTask ifDesignFileWriterTask = new IfDesignFileWriterTask(queue, fileName, objectMapper, csvWriter);
            ExecutorService executor = Executors.newFixedThreadPool(10);
            for (int i = 0; i < (response.count / response.items.size()) + 1; i++) {
                executor.submit(new IfDesignTask(i, objectMapper, client, requestBody, ifDesignFileWriterTask));
            }
            Future<?> fileWriterFuture = executor.submit(ifDesignFileWriterTask);
            try {
                fileWriterFuture.get(WRITER_TIMEOUT, TimeUnit.SECONDS);
            } catch (TimeoutException ex) {
                System.out.println("Stopping by timeout");
                fileWriterFuture.cancel(true);
            } finally {
                executor.shutdown();
                executor.awaitTermination(10, TimeUnit.SECONDS);
            }

        }

    }

}
