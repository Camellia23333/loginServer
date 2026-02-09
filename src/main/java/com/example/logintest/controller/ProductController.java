package com.example.logintest.controller;

import com.example.logintest.annotation.PreAuthorize;
import com.example.logintest.dao.ProductMapper;
import com.example.logintest.entity.Product;
import com.example.logintest.entity.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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
    /**
     * 新增商品接口
     * 只有拥有 'product:add' 权限的角色才能调用
     */
    @PreAuthorize("product:add") // <--- 看这里！一行代码搞定权限控制
    @PostMapping("/add")
    public Result<String> addProduct(@RequestBody Product product) {
        // 如果代码执行到这里，说明 AOP 已经校验通过了
        System.out.println("执行商品新增业务...");

        // 简单的参数校验
        if (product.getName() == null || product.getPrice() == null) {
            return Result.error("参数不完整");
        }

        // 补全基础信息 (这里暂时模拟，实际要有 Service 层处理)
        product.setCreateTime(new java.util.Date());
        product.setUpdateTime(new java.util.Date());
        product.setTotalStock(100);
        product.setAvailableStock(100);

        // Mybatis 插入 (需要你去 ProductMapper 补一个 insert 方法，见下一步)
        productMapper.insert(product);

        return Result.success("商品新增成功");
    }
}
