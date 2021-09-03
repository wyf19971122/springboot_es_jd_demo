package com.fiveshuai.controller;

import com.fiveshuai.service.ContentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * @Author: wyf
 * @Date: 2021/9/3 10:17
 */
@RestController
@RequestMapping("/content")
public class ContentController {

    @Autowired
    private ContentService contentService;

    //京东爬取数据放入到ES中
    @GetMapping("/putEs")
    public Boolean getDataEs(@RequestParam String keyword) throws IOException {
        return contentService.getDataToEs(keyword);
    }

    //分页查询
    @GetMapping("searchPage")
    public List<Map<String,Object>> searchPage(@RequestParam String keyword,@RequestParam Integer pageNo,@RequestParam Integer pageSize) throws IOException {
        return contentService.searchPage(keyword,pageNo,pageSize);
    }
}
