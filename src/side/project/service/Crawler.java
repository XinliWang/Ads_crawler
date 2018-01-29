package side.project.service;

import org.jsoup.*;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import side.project.model.Ads;


import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.util.*;


public class Crawler {
    private static final String AMAZON_QUERY_URL = "https://www.amazon.com/s/ref=nb_sb_ss_i_3_8?url=search-alias%3Daps&field-keywords=";
    private static final String USER_AGENT = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/51.0.2704.103 Safari/537.36";
    private final String authUser = "aaaa";
    private final String authPassword = "bbbb";
    private List<String> proxyList;
    private static HashSet<String> filter = new HashSet<>();

    private int index = 0;

    public void initProxy(String proxy_file) {
        proxyList = new ArrayList<String>();
        try (BufferedReader br = new BufferedReader(new FileReader(proxy_file))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] fields = line.split(",");
                String ip = fields[0].trim();
                proxyList.add(ip);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        Authenticator.setDefault(
                new Authenticator() {
                    @Override
                    protected PasswordAuthentication getPasswordAuthentication() {
                        return new PasswordAuthentication(authUser, authPassword.toCharArray());
                    }
                }
        );
        System.setProperty("http.proxyUser",  authUser);
        System.setProperty("http.proxyPassword",  authPassword);
        System.setProperty("socksProxyPort", "61336");  //set proxy port
    }

    private void setProxy() {
        if(index == proxyList.size()) {
            index = 0;
        }
        String proxy = proxyList.get(index);
        System.setProperty("socksProxyHost",proxy);
        index++;
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

    public List<Ads> getAmazonProds(String query, double bidPrice, int campaignID, int query_group_id, Integer numPage) {
        List<Ads> list = new ArrayList<>();

        setProxy();

        String url = AMAZON_QUERY_URL + query;
        if(numPage > 1) {
            url = url + "&page=" + numPage.toString();
        }

        try {
            HashMap<String, String> headers = new HashMap<>();
            headers.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
            headers.put("Accept-Encoding","gzip, deflate, br");
            headers.put("Accept-Language", "en-US,en;q=0.8");
            Document doc = Jsoup.connect(url).headers(headers).userAgent(USER_AGENT).maxBodySize(0).timeout(10000).get();
            Elements elements = doc.getElementsByClass("s-result-item celwidget  ");

            for(Integer i = 0; i < elements.size(); i++) {
                String id = "result_" + i.toString();
                Element prodsById = doc.getElementById(id);
                String asin = prodsById.attr("data-asin");
                if(!filter.add(asin)) {
                    Ads ad = new Ads();
                    Elements titleElementsList = prodsById.getElementsByAttribute("title");
                    for (Element titleElement : titleElementsList) {
                        String title = titleElement.attr("title");
                        if(title != null || !title.isEmpty()) {
                            ad.title = title;
                            List<String> keywords = Utility.cleanedTokenize(title);
                            ad.keyWords = keywords;
                            break;
                        }
                    }
                    ad.detail_url = "https://www.amazon.com/dp/" + asin;

                    Elements wholes =  prodsById.getElementsByClass("sx-price-whole");
                    for (Element whole : wholes) {
                        if(whole.text()!=null || !whole.text().isEmpty()) {
                            ad.price = Double.parseDouble(whole.text());
                            break;
                        }
                    }

                    Elements fractionals =  prodsById.getElementsByClass("sx-price-fractional");
                    for (Element fractional : fractionals) {
                        if(fractional.text()!=null || !fractional.text().isEmpty()) {
                            ad.price += Double.parseDouble(fractional.text()) * 0.01;
                            break;
                        }
                    }

                    Element categoryElement = doc.selectFirst("#leftNavContainer > ul:nth-child(3) > div > li:nth-child(1) > span > a > h4");
                    ad.category = categoryElement.text();
                    ad.bidPrice = bidPrice;
                    ad.campaignId = campaignID;
                    ad.query_group_id = query_group_id;
                    list.add(ad);
                }
            }
        }catch (IOException e) {
            e.printStackTrace();
        }
        return list;
    }
}
