package side.project;

import org.codehaus.jackson.map.ObjectMapper;
import side.project.model.Ads;
import side.project.service.Crawler;
import side.project.service.Utility;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Main {

    public static void main(String[] args) {
        String FEEDS_URL = args[0];
        String OUTPUT_URL = args[1];

        Crawler crawler = new Crawler();
        File file = new File(OUTPUT_URL);
        ObjectMapper mapper = new ObjectMapper();

        try(BufferedReader br = new BufferedReader(new FileReader(FEEDS_URL))){
            String line = br.readLine().trim();
            while(line != null) {
                if(line.length() > 0) {
                    String[] info = line.split("\\s*,\\s*");
                    String query = info[0];
                    double bidPrice = Double.parseDouble(info[1]);
                    int compaignID = Integer.parseInt(info[2]);
                    int query_group_id = Integer.parseInt(info[3]);
                    for(Integer numPage = 1; numPage < 10; numPage++) {
                        List<Ads> list = crawler.getAmazonProds(query, bidPrice, compaignID, query_group_id, numPage);
                        mapper.writeValue(file, list);
                    }

                }
            }
        }catch (IOException e) {
            e.printStackTrace();
        }
    }
}