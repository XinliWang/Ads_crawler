package side.project.service;

import side.project.model.Ads;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import org.codehaus.jackson.map.ObjectMapper;

public class Utility {
    private static final String RESOURCE_URL = "/Users/xinliwang/Documents/resource/crawler/rawQuery3.txt";
    private static final String OUTPUT_URL = "/Users/xinliwang/Documents/resource/crawler/ads.txt";

    public List<Ads> readFile() {
        BufferedReader br = null;
        List<Ads> list = new ArrayList<>();
        try{
            br = new BufferedReader(new FileReader(RESOURCE_URL));
            StringBuilder sb = new StringBuilder();
            String line = br.readLine().trim();
            while (line != null) {
                if(line.length() > 0) {
                    Ads ad = new Ads();
                    String[] strings = line.split("\\s*,\\s*");
                    ad.query = strings[0];
                    ad.bidPrice = Double.parseDouble(strings[1]);
                    ad.campaignId = Integer.parseInt(strings[2]);
                    ad.query_group_id = Integer.parseInt(strings[3]);
                    list.add(ad);
                    line = br.readLine();
                }
            }
        }catch (FileNotFoundException e1) {
            e1.printStackTrace();
        }catch (IOException e2) {
            e2.printStackTrace();
        }finally {
            try{
                br.close();
            }catch (IOException e) {
                e.printStackTrace();
            }

        }
        return list;
    }

    public void outputFile(List<Ads> list) {
        File file = new File(OUTPUT_URL);

        ObjectMapper mapper = new ObjectMapper();
        try {
            mapper.writeValue(file, list);
        }catch (IOException e) {
            e.printStackTrace();
        }

    }

    public List<Ads> readFile1() {
        List<Ads> list = new ArrayList<>();
        Ads ad = new Ads();
        ad.query = "home theater system";
        list.add(ad);
        return list;
    }

}
