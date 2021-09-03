package com.fiveshuai.utils;

import com.fiveshuai.pojo.Content;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * @Author: wyf
 * @Date: 2021/9/2 15:27
 */
@Component
public class HtmlParseUtils {

    public static void main(String[] args) throws IOException {
        new HtmlParseUtils().parseJD("java").forEach(System.out::println);
    }

    public  List<Content> parseJD(String keywords) throws IOException {
        //获取请求 https://search.jd.com/Search?keyword=java
        //前提一定要联网
        String url = "https://search.jd.com/Search?keyword="+keywords;

        //此document就是浏览器的document对象，js中方法这里都可以使用
        Document document = Jsoup.parse(new URL(url), 30000);
        Element element = document.getElementById("J_goodsList");
        Elements elements = element.getElementsByTag("li");
        ArrayList<Content> goodLists = new ArrayList<>();
        //el就是每一个li标签
        for (Element el : elements) {
            //关于图片特别多的网站，图片的都是延迟加载的，懒加载
            String price = el.getElementsByClass("p-price").eq(0).text();
            String title = el.getElementsByClass("p-name").eq(0).text();
            Content content = new Content();
            content.setPrice(price);
            content.setTitle(title);
            goodLists.add(content);
        }
        return goodLists;
    }
}
