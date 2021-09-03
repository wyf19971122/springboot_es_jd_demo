package com.fiveshuai.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * @Author: wyf
 * @Date: 2021/9/1 14:24
 */
@Component
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    private String name;

    private Integer age;
}
