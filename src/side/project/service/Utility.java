package side.project.service;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.core.StopFilter;
import org.apache.lucene.analysis.en.KStemFilter;
import org.apache.lucene.analysis.standard.StandardFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.util.CharArraySet;
import side.project.model.Ads;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.codehaus.jackson.map.ObjectMapper;

import org.apache.lucene.util.Version;

public class Utility {
    private static final String RESOURCE_URL = "/Users/xinliwang/Documents/resource/crawler/rawQuery3.txt";
    private static final String OUTPUT_URL = "/Users/xinliwang/Documents/resource/crawler/ads.txt";

    private static final Version LUCENE_VERSION = Version.LUCENE_40;
    private static List<? extends Serializable> symbolStopWords = Arrays.asList('.', ',', '"', "'", '?', '!', ':', ';', '(', ')', '[', ']', '{', '}', '&','/', "...",'-','+','*','|',"),");
    private static CharArraySet symbolStopSet = new CharArraySet(Version.LUCENE_40, symbolStopWords,true);
    private static String stopWords = "a,able,about,across,after,all,almost,also,am,among,an,and,any,are,as,at,be,because,been,but,by,can,cannot,could,dear,did,do,does,either,else,ever,every,for,from,get,got,had,has,have,he,her,her,him,his,how,however,i,if,in,into,is,it,its,just,least,let,like,likely,may,me,might,most,must,my,neither,no,nor,not,of,off,often,on,only,or,other,our,own,rather,said,say,says,she,should,since,so,some,than,that,the,their,them,then,there,these,they,this,tis,to,too,twas,us,wants,was,we,were,what,when,where,which,while,who,whom,why,will,with,would,yet,you,your";
    private static CharArraySet getStopwords(String stopwords) {
        List<String> stopwordsList = new ArrayList<>();
        for(String stop : stopwords.split(",")) {
            stopwordsList.add(stop.trim());
        }
        return new CharArraySet(LUCENE_VERSION, stopwordsList, true);
    }
    public static List<String> cleanedTokenize(String input) {
        List<String> tokens = new ArrayList<>();
        StringReader reader = new StringReader(input.toLowerCase());
        Tokenizer tokenizer = new StandardTokenizer(LUCENE_VERSION, reader);
        TokenStream tokenStream = new StandardFilter(LUCENE_VERSION, tokenizer);
        CharArraySet stopSet = getStopwords(stopWords);
        stopSet.add(symbolStopSet);
        tokenStream = new StopFilter(LUCENE_VERSION, tokenStream, stopSet);
        tokenStream = new KStemFilter(tokenStream);
        StringBuilder sb = new StringBuilder();
        CharTermAttribute charTermAttribute = tokenizer.addAttribute(CharTermAttribute.class);
        try {
            tokenStream.reset();
            while(tokenStream.incrementToken()) {
                String term = charTermAttribute.toString();
                tokens.add(term);
                sb.append(term + " ");
            }
            tokenStream.end();
            tokenStream.close();
            tokenizer.close();
        }catch (IOException e) {
            e.printStackTrace();
        }
        return tokens;
    }






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
