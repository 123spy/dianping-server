package com.spy.server.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.spy.server.model.domain.Category;
import com.spy.server.service.CategoryService;
import com.spy.server.mapper.CategoryMapper;
import org.springframework.stereotype.Service;

/**
* @author OUC
* @description 针对表【category(店铺分类表)】的数据库操作Service实现
* @createDate 2026-03-20 19:51:22
*/
@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category>
    implements CategoryService{

}




