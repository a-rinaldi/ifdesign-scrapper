package patrick.extractor.scraper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import patrick.extractor.scraper.response.CompanyResponse;

public class IfDesignFileWriterTask implements Runnable {

    private final BlockingQueue<CompanyResponse> queue;
    private final String fileName;
    private final ObjectMapper objectMapper;
    private volatile boolean stop = false;
    private final ObjectWriter csvWriter;

    public IfDesignFileWriterTask(BlockingQueue<CompanyResponse> queue, String fileName, ObjectMapper objectMapper, ObjectWriter csvWriter) {
        this.queue = queue;
        this.objectMapper = objectMapper;
        this.fileName = fileName;
        this.csvWriter = csvWriter;
    }

    @Override
    public void run() {
        try ( FileWriter writer = new FileWriter(fileName, true)) {
            while (!stop) {
                try {
                    CompanyResponse response = queue.take();
                    /*if using writeValue(writer, response.items) csvMapper closes the stream*/
                    String csvValue = csvWriter.writeValueAsString(response.items);
                    writer.write(csvValue);
                } catch (InterruptedException e) {
                    break;
                } catch (JsonProcessingException ex) {
                    throw new UnsupportedOperationException(ex.getMessage());
                } catch (IOException ex) {
                    throw new UnsupportedOperationException(ex.getMessage());
                }
            }
        } catch (IOException ex) {
            throw new UnsupportedOperationException(ex.getMessage());
        }
    }

    public void stop() {
        stop = true;
    }
    
    public BlockingQueue<CompanyResponse> getQueue()
    {
        return queue;
    }
    
}
