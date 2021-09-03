package com.fiveshuai.service;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * @Author: wyf
 * @Date: 2021/9/3 10:18
 */
public interface ContentService {
    Boolean getDataToEs(String keyword) throws IOException;

    List<Map<String, Object>> searchPage(String keyword,int pageNo,int pageSize) throws IOException;
}
