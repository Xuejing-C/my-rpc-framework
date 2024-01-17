package com.rpc.api;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 测试实体
 * */
@AllArgsConstructor
@NoArgsConstructor
@Data
public class HelloObject implements Serializable {
    private Integer id;
    private String message;
}
