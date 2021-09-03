package com.fiveshuai;

import com.alibaba.fastjson.JSON;
import com.fiveshuai.entity.User;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.MatchAllQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.FetchSourceContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

/**
 * es7.6.2 高级客户端测试API
 */
@SpringBootTest
class EsApiApplicationTests {

    @Autowired
    @Qualifier("restHighLevelClient")
    private RestHighLevelClient client;

    //测试索引的创建 request
    @Test
    public void createIndex() throws IOException {
        //1.创建索引请求
        CreateIndexRequest request = new CreateIndexRequest("fiveshuai_1");
        //2.客户端执行请求indicesClient,请求后获得相应信息
        CreateIndexResponse response = client.indices().create(request, RequestOptions.DEFAULT);
        System.out.println(response);
    }

    //判断是否存在索引
    @Test
    public void existIndex() throws IOException {
        GetIndexRequest request = new GetIndexRequest("fiveshuai_1");
        boolean exists = client.indices().exists(request, RequestOptions.DEFAULT);
        System.out.println(exists);
    }

    //删除索引
    @Test
    public void removeIndex() throws IOException {
        DeleteIndexRequest request = new DeleteIndexRequest("fiveshuai_1");
        AcknowledgedResponse response = client.indices().delete(request, RequestOptions.DEFAULT);
        System.out.println(response.isAcknowledged());
    }

    //添加文档
    @Test
    public void addDocument() throws IOException {
        User user = new User("fiveshuai", 18);
        IndexRequest request = new IndexRequest("fiveshuai_1");

        //设置规则 put /fiveshuai/_doc/1
        request.id("1");
        request.timeout(TimeValue.timeValueSeconds(1));

        //将我们的数据放入请求
        request.source(JSON.toJSONString(user), XContentType.JSON);
        //客户端发送请求,获取相应的结果
        IndexResponse response = client.index(request, RequestOptions.DEFAULT);
        //打印返回状态
        System.out.println(response.status());
    }

    //获取文档，判断文档是否存在
    @Test
    public void existDocument() throws IOException {
        GetRequest request = new GetRequest("fiveshuai_1", "1");
        //过滤_source的上下文，提高效率
        request.fetchSourceContext(new FetchSourceContext(false));
        request.storedFields("_none_");
        boolean exists = client.exists(request, RequestOptions.DEFAULT);
        System.out.println(exists);
    }

    //获取文档
    @Test
    public void getDocument() throws IOException {
        GetRequest request = new GetRequest("fiveshuai_1", "1");
        GetResponse response = client.get(request, RequestOptions.DEFAULT);
        Map<String, Object> res = response.getSourceAsMap();
        System.out.println(res);
    }

    //更新文档
    @Test
    public void updateDocument() throws IOException {
        UpdateRequest request = new UpdateRequest("fiveshuai_1", "1");
        request.timeout("1s");
        User user = new User("fiveshuai", 19);
        request.doc(JSON.toJSONString(user), XContentType.JSON);
        UpdateResponse response = client.update(request, RequestOptions.DEFAULT);
        System.out.println(response.status());
        System.out.println(response);
    }

    //删除文档记录
    @Test
    public void deleteDocument() throws IOException {
        DeleteRequest request = new DeleteRequest("fiveshuai_1", "1");
        DeleteResponse delete = client.delete(request, RequestOptions.DEFAULT);
        System.out.println(delete.status());
    }

    //批量插入
    @Test
    public void addBulkDocument() throws IOException {
        BulkRequest bulkRequest = new BulkRequest();
        bulkRequest.timeout(TimeValue.timeValueSeconds(10));

        ArrayList<User> userList = new ArrayList<>();
        userList.add(new User("Paul", 36));
        userList.add(new User("James", 35));
        userList.add(new User("Durant", 33));
        userList.add(new User("Curry", 32));
        //批处理请求
        for (int i = 0; i < userList.size(); i++) {
            bulkRequest.add(new IndexRequest("fiveshuai_2")
                    .id("" + (i + 1))
                    .source(JSON.toJSONString(userList.get(i)), XContentType.JSON));
        }
        BulkResponse responses = client.bulk(bulkRequest, RequestOptions.DEFAULT);
        System.out.println(responses.hasFailures());//返回false为成功
    }

    //查询
    @Test
    public void searchDocument() throws IOException {
        SearchRequest request = new SearchRequest("fiveshuai_2");
        //构建搜索条件
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        //使用QueryBuilders快速匹配
        //精确匹配
//		TermQueryBuilder termQueryBuilder = QueryBuilders.termQuery("name", "Paul");
        //匹配全部查询
        MatchAllQueryBuilder matchAllQueryBuilder = QueryBuilders.matchAllQuery();

        sourceBuilder.query(matchAllQueryBuilder);
        sourceBuilder.timeout(TimeValue.timeValueSeconds(60));

        SearchResponse search = client.search(request, RequestOptions.DEFAULT);
        System.out.println(JSON.toJSONString(search.getHits()));

        for (SearchHit hit : search.getHits().getHits()) {
            System.out.println(hit.getSourceAsString());
        }

    }
}
