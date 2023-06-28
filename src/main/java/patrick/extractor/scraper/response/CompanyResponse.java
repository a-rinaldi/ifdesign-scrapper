package patrick.extractor.scraper.response;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.util.List;
import patrick.extractor.scraper.deserializer.ArrayToStringDeserializer;


public class CompanyResponse extends GenericResponse {
    
    public List<CompanyItem> items;
    
    public static class CompanyItem {
        
        /*other infos are available*/
        
        public String companyName;
        public String email;
        public String logo;
        public String website;
        public String phone;
        public String headquarter;
        public String city;
        public String postCode;
        public String country;
        public String addressLine1;
        public String addressLine2;
        public String facebook;
        public String instagram;
        public String pinterest;
        public String linkedIn;
        public String youTube;
        public String twitter;
        public Integer founded;
        public String region;
        public String locations;
        @JsonDeserialize(using = ArrayToStringDeserializer.class)
        public String hashtags;
        
    }
    
}
