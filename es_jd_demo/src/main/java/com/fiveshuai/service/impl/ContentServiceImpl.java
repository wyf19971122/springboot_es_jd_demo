package com.fiveshuai.service.impl;

import com.alibaba.fastjson.JSON;
import com.fiveshuai.pojo.Content;
import com.fiveshuai.service.ContentService;
import com.fiveshuai.utils.HtmlParseUtils;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @Author: wyf
 * @Date: 2021/9/3 10:18
 */
@Service
public class ContentServiceImpl implements ContentService {

    @Autowired
    private RestHighLevelClient restHighLevelClient;

    //解析京东数据放到es中
    @Override
    public Boolean getDataToEs(String keyword) throws IOException {
        //创建索引库
        CreateIndexRequest indexRequest = new CreateIndexRequest("jd_goods");
        restHighLevelClient.indices().create(indexRequest, RequestOptions.DEFAULT);
        List<Content> contents = new HtmlParseUtils().parseJD(keyword);
        //把查询出来的数据插入es中
        BulkRequest request = new BulkRequest();
        request.timeout(TimeValue.timeValueSeconds(3000));
        for (int i = 0; i < contents.size(); i++) {
            request.add(new IndexRequest("jd_goods")
            .id(""+i)
            .source(JSON.toJSONString(contents.get(i)), XContentType.JSON));
        }
        BulkResponse bulk = restHighLevelClient.bulk(request, RequestOptions.DEFAULT);
        return !bulk.hasFailures();
    }

    @Override
    public List<Map<String, Object>> searchPage(String keyword, int pageNo, int pageSize) throws IOException {
        if (pageNo <= 1){
            pageNo = 1;
        }
        //条件检索
        SearchRequest searchRequest = new SearchRequest("jd_goods");
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        TermQueryBuilder termQueryBuilder = QueryBuilders.termQuery("title", keyword);
        sourceBuilder.query(termQueryBuilder);
        sourceBuilder.timeout(TimeValue.timeValueSeconds(60));
        sourceBuilder.from(pageNo);
        sourceBuilder.size(pageSize);

        //高亮
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        highlightBuilder.field("title");
        highlightBuilder.requireFieldMatch(false);//多个高亮显示
        highlightBuilder.preTags("<span style ='color:red'>");
        highlightBuilder.postTags("</span>");
        sourceBuilder.highlighter(highlightBuilder);
        //执行搜索
        searchRequest.source(sourceBuilder);
        SearchResponse search = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        //解析响应结果
        ArrayList<Map<String, Object>> list = new ArrayList<>();
        for (SearchHit hit : search.getHits().getHits()) {

            Map<String, HighlightField> highlightFields = hit.getHighlightFields();
            HighlightField title = highlightFields.get("title");
            Map<String, Object> sourceAsMap = hit.getSourceAsMap();
            //解析替换高亮字段
            if (title != null){
                Text[] fragments = title.fragments();
                String n_title = "";
                for (Text text : fragments){
                    n_title += text;
                }
                sourceAsMap.put("title",n_title);
            }
            list.add(hit.getSourceAsMap());
        }
        return list;
    }
}
