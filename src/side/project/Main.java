package side.project;

import side.project.model.Ads;
import side.project.service.Crawler;
import side.project.service.Utility;

import java.util.ArrayList;
import java.util.List;

public class Main {

    public static void main(String[] args) {
        String url = "https://www.amazon.com/gp/product/B0749WVS7J/ref=s9_acss_bw_cg_EchoCP_2b1_w?pf_rd_m=ATVPDKIKX0DER&pf_rd_s=merchandised-search-3&pf_rd_r=KKCCRR6ZYQKD7AQPHWMN&pf_rd_t=101&pf_rd_p=f9078236-9dcd-4ef3-a76d-98f7374b73ac&pf_rd_i=9818047011";
        String url1 = "https://www.amazon.com/Habits-Highly-Effective-People-Powerful-ebook/dp/B01069X4H0?pf_rd_m=ATVPDKIKX0DER&pf_rd_p=3445487922&pf_rd_r=3a7f3f40-8898-4b3b-ae12-1e48f824099d&pd_rd_wg=v7IQw&pf_rd_s=desktop-gateway&pf_rd_t=40701&pd_rd_w=Vcq6p&pf_rd_i=desktop-gateway&pd_rd_r=3a7f3f40-8898-4b3b-ae12-1e48f824099d&ref_=pd_gw_nn_ebooks";

//        crawlerLogic.getAmazonProd(url1);
        Crawler crawler = new Crawler();
//        crawler.getAmazonProds("apple earphones");
//        crawler.initProxy();
//        crawler.testProxy();

        Utility utility = new Utility();
        List<Ads> list = utility.readFile1();
        List<Ads> result = new ArrayList<>();
        for(int i = 0; i < list.size(); i++) {
            Ads ad = list.get(i);
            List<Ads> adsList = crawler.getAmazonProds(ad);
            result.addAll(adsList);
        }
        utility.outputFile(result);
    }
}