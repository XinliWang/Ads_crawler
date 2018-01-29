package side.project.service;

import org.jsoup.*;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import side.project.model.Ads;


import java.io.*;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.util.*;


public class Crawler {
    private static final String AMAZON_QUERY_URL = "https://www.amazon.com/s/ref=nb_sb_ss_i_3_8?url=search-alias%3Daps&field-keywords=";
    private static final String USER_AGENT = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/51.0.2704.103 Safari/537.36";
    private final String authUser = "aaaa";
    private final String authPassword = "bbbb";
    private List<String> proxyList;
    private static HashSet<String> filter;

    BufferedWriter logWriter;

    private int index = 0;
    private int adId;

    public Crawler(String proxy_file, String log_file) {
        filter = new HashSet<>();
        adId = 2000;
        initProxy(proxy_file);
        initLog(log_file);
    }

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

    //setup log
    private void initLog(String log_path) {
        try {
            File log = new File(log_path);
            if(!log.exists()) {
                log.createNewFile();
            }
            FileWriter fw = new FileWriter(log.getAbsoluteFile());
            logWriter = new BufferedWriter(fw);

        }catch (IOException e) {
            e.printStackTrace();
        }
    }

    //Original: https://www.amazon.com/BEACOO-Charging-Station-AirPods-NightStand/dp/B077CZL5FB/ref=sr_1_31?ie=UTF8&qid=1517250001&sr=8-31&keywords=airpods
    //Cleaned: https://www.amazon.com/BEACOO-Charging-Station-AirPods-NightStand/dp/B077CZL5FB
    private String cleanedUrl(String url) {
        int i = url.indexOf("ref");
        return url.substring(0, i - 1);
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
            if(elements.size() == 0) {
                logWriter.write("0 result for query:" + query + " , page number is " + numPage);
                logWriter.newLine();
            }

            for(Integer i = 0; i < elements.size(); i++) {
                String id = "result_" + i.toString();
                Element prodsById = doc.getElementById(id);
                String asin = prodsById.attr("data-asin");

                if(!filter.add(asin)) {
                    Ads ad = new Ads();

                    //get title and keywords
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

                    //get detail url
                    String detail_path_select = " > div > div > div > div.a-fixed-left-grid-col.a-col-left > div > div > a";
                    String detail_path = "#" + id + detail_path_select;
                    Element detail_path_element = doc.select(detail_path).first();
                    if(detail_path_select != null) {
                        String orignal_detail_url = detail_path_element.attr("href");
                        String detail_url = cleanedUrl(orignal_detail_url);
                        logWriter.write("crawled url: " + detail_url);
                        logWriter.newLine();
                        ad.detail_url = detail_url;
                    }

                    if(ad.detail_url == null || ad.detail_url == "") {
                        logWriter.write("cannot parse detail for query:" + query );
                        logWriter.newLine();
                        continue;
                    }

                    //get price
                    ad.price = 0.0;
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
                    //get category
                    Element categoryElement = doc.select("#leftNavContainer > ul:nth-child(3) > div > li:nth-child(1) > span > a > h4").first();
                    if(categoryElement == null) {
                        logWriter.write("cannot parse category for query:" + query + ", title: " + ad.title);
                        logWriter.newLine();
                        continue;
                    }
                    ad.category = categoryElement.text();
                    ad.adId = adId;
                    adId++;
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
