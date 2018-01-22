package side.project.model;

import java.io.Serializable;
import java.util.List;

public class Ads implements Serializable, Cloneable{
    private static final long serialVersionUID = 1L;
    public int adId;
    public int campaignId;
    public List<String> keyWords;
    public double relevanceScore;  //0
    public double pClick;   //0
    public double bidPrice;
    public double rankScore;   //0
    public double qualityScore;   //0
    public double costPerClick;   //0
    public int position;   //0
    public String title;
    public double price;
    public String thumbnail;
    public String description;  //null
    public String brand;
    public String detail_url;
    public String query;
    public int query_group_id;
    public String category;

    @Override
    public Object clone() {
        Ads ad = null;
        try{
            ad = (Ads)super.clone();
        }catch(CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return ad;
    }

    public String toString() {
        return "campaign id:" + campaignId + " bidPrice: " + bidPrice + " title: " + title + " price: " + price + " detail_url: " + detail_url + " query: " + query + " query_group_id: " + query_group_id;
    }

}
