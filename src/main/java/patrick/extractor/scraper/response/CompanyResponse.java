package patrick.extractor.scraper.response;

import java.util.List;


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
    }
    
}
