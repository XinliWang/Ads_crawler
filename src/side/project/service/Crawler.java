package side.project.service;

import org.jsoup.*;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import side.project.model.Ads;


import java.io.IOException;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.util.*;


public class Crawler {
    private static final String AMAZON_QUERY_URL = "https://www.amazon.com/s/ref=nb_sb_ss_i_3_8?url=search-alias%3Daps&field-keywords=";
    private static final String USER_AGENT = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/51.0.2704.103 Safari/537.36";
    private final String authUser = "aaaa";
    private final String authPassword = "bbbb";
    private static HashSet<String> filter = new HashSet<>();
    public void initProxy() {
        System.setProperty("socksProxyHost",  "199.101.97.161");
        System.setProperty("socksProxyPort", "61336");
        Authenticator.setDefault(
                new Authenticator() {
                    @Override
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(authUser, authPassword.toCharArray());
                    }
                }
        );
    }

    public void testProxy() {
        String url = "http://www.toolsvoid.com/what-is-my-ip-address";
        try {
            HashMap<String, String> headers = new HashMap<>();
            headers.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
            headers.put("Accept-Encoding", "gzip, deflate, br");
            headers.put("Accept-Language", "en-US,en;q=0.8");
            Document doc = Jsoup.connect(url).headers(headers).userAgent(USER_AGENT).maxBodySize(0).timeout(10000).get();
            String ip = doc.select("body > section.articles-section > div > div > div > div.col-md-8.display-flex > div > div.table-responsive > table > tbody > tr:nth-child(1) > td:nth-child(2) > strong").text();
            System.out.println(ip);
        }catch (IOException e) {
            e.printStackTrace();
        }
    }
    public List<Ads> getAmazonProds(Ads ad) {
        String query = ad.query;
        String url = AMAZON_QUERY_URL + query;
        List<Ads> list = new ArrayList<>();
        try {
            HashMap<String, String> headers = new HashMap<>();
            headers.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
            headers.put("Accept-Encoding", "gzip, deflate, br");
            headers.put("Accept-Language", "en-US,en;q=0.8");
            Document doc = Jsoup.connect(url).headers(headers).userAgent(USER_AGENT).maxBodySize(0).timeout(10000).get();
            Elements element = doc.getElementsByClass("s-result-item celwidget  ");
            System.out.println(element.size());

            for(Integer i=0; i < element.size(); i++) {
                String id = "result_" + i.toString();
                Element prodsById = doc.getElementById(id);

                String asin = prodsById.attr("data-asin");
                System.out.println("asin:" + asin);
                if(filter.add(asin)) {
                    Ads newAd = (Ads)ad.clone();

                    Elements titleEleList = prodsById.getElementsByAttribute("title");
                    System.out.println(titleEleList.size());
                    for(Element titleEle : titleEleList) {
                        String title =  titleEle.attr("title");
                        newAd.title = title;
                        System.out.println("title:" + title);
                        List<String> keywords = Arrays.asList(title.split(" "));
                        newAd.keyWords = keywords;
                    }
                    newAd.detail_url = "https://www.amazon.com/dp/" + asin;
                    Elements wholes =  prodsById.getElementsByClass("sx-price-whole");
                    String whole_price = "0";
                    for(Element whole : wholes) {
                        whole_price = whole.text();
                    }
                    Elements fractionals =  prodsById.getElementsByClass("sx-price-fractional");
                    String fractional_price = "0";
                    for(Element fractional : fractionals) {
                        fractional_price = fractional.text();
                    }
                    String price = whole_price + "." + fractional_price;
                    System.out.println("Price Load: " + price);
                    newAd.price = Double.parseDouble(price);
                    System.out.println(newAd.toString());
                    list.add(newAd);
                }


            }
        }catch (IOException e) {
            e.printStackTrace();
        }
        return list;
    }
}
