package com.example.logintest.controller;

import com.example.logintest.dao.ProductMapper;
import com.example.logintest.entity.Product;
import com.example.logintest.entity.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
@RestController
@RequestMapping("/api/product")
public class ProductController {

    @Autowired
    private ProductMapper productMapper;

    /**
     * 新增: 获取商品列表接口
     */
    @GetMapping("/list")
    public Result<List<Product>> getProductList() {
        List<Product> list = productMapper.findAll();
        return Result.success("获取成功", list);
    }

    /**
     * 获取商品详情,前端展示最新库存
     */
    @GetMapping("/{id}")
    public Result<Product> getProduct(@PathVariable Long id) {
        Product product = productMapper.findById(id);
        if (product == null) {
            return Result.error("商品不存在");
        }
        return Result.success("获取成功", product);
    }
}
