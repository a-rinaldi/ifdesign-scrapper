package patrick.extractor.scraper.request;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Stream;

public class ApiRequest {

    public List<String> businessSectors = new LinkedList();

    public List<String> countries = new LinkedList();

    public List<String> fieldsOfWork = new LinkedList();

    public String find = "";

    public List<Integer> types = new LinkedList(); //1 company, 2 design studios

    public List<Integer> years = new LinkedList();

    public static ApiRequest of(Integer... types) {
        ApiRequest instance = new ApiRequest();
        Stream.of(types).forEach(type -> {
            instance.types.add(type);
        });
        return instance;
    }

}
