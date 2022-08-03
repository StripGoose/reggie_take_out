package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.R;
import com.itheima.reggie.dto.DishDto;
import com.itheima.reggie.entity.Category;
import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.entity.DishFlavor;
import com.itheima.reggie.service.CategoryService;
import com.itheima.reggie.service.DishFlavorService;
import com.itheima.reggie.service.DishService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 菜品管理
 */
@RestController
@RequestMapping("/dish")
@Slf4j
@Api(tags = "菜品管理接口")
public class DishController {

    @Autowired
    private DishService dishService;

    @Autowired
    private DishFlavorService dishFlavorService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 新增菜品
     * @param dishDto
     * @return
     */
    @PostMapping
    @CacheEvict(value = "dishCache",allEntries = true)
    @ApiOperation(value = "新增菜品")
    public R<String> save(@RequestBody DishDto dishDto){
        log.info(dishDto.toString());

        dishService.saveWithFlavor(dishDto);

        //清理所有菜品的缓存数据
        //Set keys = redisTemplate.keys("dish_*");
        //redisTemplate.delete(keys);

        //清理某个分类下面的菜品缓存数据
        //String key = "dish_" + dishDto.getCategoryId() + "_1";
        //redisTemplate.delete(key);

        return R.success("新增菜品成功");
    }

    /**
     * 菜品信息分页查询
     * @param page
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    @Cacheable(value = "dishCache",key = "'page' + #page + '_' + #pageSize + '_' + #name")
    @ApiOperation(value = "菜品信息分页查询")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "page",value = "页码",required = true),
            @ApiImplicitParam(name = "pageSize",value = "每页记录数",required = true),
            @ApiImplicitParam(name = "name",value = "菜品名称",required = false)
    })
    public R<Page> page(int page,int pageSize,String name){

        //构造分页构造器对象
        Page<Dish> pageInfo = new Page<>(page, pageSize);
        Page<DishDto> dishDtoPage = new Page<>();

        //条件构造器
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        //添加过滤条件
        queryWrapper.like(name != null,Dish::getName,name);
        //添加排序条件
        queryWrapper.orderByDesc(Dish::getUpdateTime);

        //执行分页查询
        dishService.page(pageInfo,queryWrapper);

        //对象拷贝 除了records(数据的集合)，其他的都拷贝 pageInfo --> dishDtoPage
        BeanUtils.copyProperties(pageInfo,dishDtoPage,"records");
        //拿出数据集合
        List<Dish> records = pageInfo.getRecords();
        //数据循环
        List<DishDto> list = records.stream().map((item) -> {
            DishDto dishDto = new DishDto();
            //records数据循环拷贝给dishDto
            BeanUtils.copyProperties(item,dishDto);

            Long categoryId = item.getCategoryId();//分类id
            //根据id查询分类对象,让数据插入categoryName
            Category category = categoryService.getById(categoryId);
            if (category != null){
                String categoryName = category.getName();
                dishDto.setCategoryName(categoryName);
            }

            return dishDto;
        }).collect(Collectors.toList());//最后全部收集转List字符串

        dishDtoPage.setRecords(list);

        return R.success(dishDtoPage);
    }

    /**
     * 根据id查询菜品信息和对应的口味信息
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    @Cacheable(value = "dishCache",key = "'get' + #id")
    @ApiOperation(value = "根据id查询菜品信息和对应的口味信息")
    @ApiImplicitParam(name = "id",value = "id",required = true)
    public R<DishDto> get(@PathVariable Long id){
        DishDto dishDto = dishService.getByIdWithFlavor(id);
        return R.success(dishDto);
    }

    /**
     * 修改菜品
     * @param dishDto
     * @return
     */
    @PutMapping
    @CacheEvict(value = "dishCache",allEntries = true)
    @ApiOperation(value = "修改菜品")
    public R<String> update(@RequestBody DishDto dishDto){
        log.info(dishDto.toString());

        dishService.updateWithFlavor(dishDto);

        //清理所有菜品的缓存数据
        //Set keys = redisTemplate.keys("dish_*");
        //redisTemplate.delete(keys);

        //清理某个分类下面的菜品缓存数据
        //String key = "dish_" + dishDto.getCategoryId() + "_1";
        //redisTemplate.delete(key);

        return R.success("修改菜品成功");
    }

    /**
     * 根据条件查询对应的菜品数据
     * @param dish
     * @return
     */
    /*public R<List<Dish>> list(Dish dish){

        //构造查询条件
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(dish.getCategoryId() != null , Dish::getCategoryId,dish.getCategoryId());
        //添加条件，查询状态为1(起售状态)的菜品
        queryWrapper.eq(Dish::getStatus,1);

        //添加排序条件
        queryWrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);

        List<Dish> list = dishService.list(queryWrapper);
        return R.success(list);
    }*/
    @GetMapping("/list")
    @Cacheable(value = "dishCache",key = "'list' + #dish.categoryId + '_' + #dish.status")
    @ApiOperation(value = "根据条件查询对应的菜品数据")
    public R<List<DishDto>> list(Dish dish){
        //List<DishDto> dishDtoList = null;
        //动态构造key
        //String key = "dish_" + dish.getCategoryId() + "_" + dish.getStatus();//dish_xxxxxxxxx_1
        //先从redis中获取缓存数据
        //dishDtoList = (List<DishDto>) redisTemplate.opsForValue().get(key);
        /*if (dishDtoList != null){
            //如果存在，直接返回，无需查询数据库
            return R.success(dishDtoList);
        }*/

        //构造查询条件
        LambdaQueryWrapper<Dish> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(dish.getCategoryId() != null , Dish::getCategoryId,dish.getCategoryId());
        //添加条件，查询状态为1(起售状态)的菜品
        queryWrapper.eq(Dish::getStatus,1);

        //添加排序条件
        queryWrapper.orderByAsc(Dish::getSort).orderByDesc(Dish::getUpdateTime);

        List<Dish> list = dishService.list(queryWrapper);

        List<DishDto> dishDtoList = list.stream().map((item) -> {
            DishDto dishDto = new DishDto();
            //records数据循环拷贝给dishDto
            BeanUtils.copyProperties(item,dishDto);

            Long categoryId = item.getCategoryId();//分类id
            //根据id查询分类对象,让数据插入categoryName
            Category category = categoryService.getById(categoryId);
            if (category != null){
                String categoryName = category.getName();
                dishDto.setCategoryName(categoryName);
            }

            //当前菜品的id
            Long dishId = item.getId();
            LambdaQueryWrapper<DishFlavor> lambdaQueryWrapper = new LambdaQueryWrapper<>();
            lambdaQueryWrapper.eq(DishFlavor::getDishId,dishId);
            //SQL:select * from dish_flavor where dish_id = ?
            List<DishFlavor> dishFlavorList = dishFlavorService.list(lambdaQueryWrapper);
            dishDto.setFlavors(dishFlavorList);
            return dishDto;
        }).collect(Collectors.toList());

        //如果不存在，走数据库，将查询到的菜品数据缓存到Redis，超时60分钟自动清理
        //redisTemplate.opsForValue().set(key,dishDtoList,60, TimeUnit.MINUTES);

        return R.success(dishDtoList);
    }

    /**
     * 删除菜品
     * @param ids
     * @return
     */
    @DeleteMapping
    @CacheEvict(value = "dishCache",allEntries = true)
    @ApiOperation(value = "删除菜品")
    @ApiImplicitParam(name = "ids",value = "已选的菜品id",required = true)
    public R<String> delete(@RequestParam List<Long> ids){
        log.info("ids:{}",ids);

        dishService.deleteWithFlavor(ids);

        return R.success("菜品数据删除成功");
    }

    /**
     * 修改菜品状态
     * @param status
     * @param ids
     * @return
     */
    @PostMapping("/status/{status}")
    @CacheEvict(value = "dishCache",allEntries = true)
    @ApiOperation(value = "修改菜品状态")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "status",value = "状态",required = true),
            @ApiImplicitParam(name = "ids",value = "已选的菜品id",required = true),
    })
    public R<String> status(@PathVariable int status,@RequestParam List<Long> ids){
        //update dish set status=0/1 where id in (1,2,3)
        //条件构造器
        UpdateWrapper<Dish> updateWrapper = new UpdateWrapper<>();
        updateWrapper.lambda().in(Dish::getId,ids).set(Dish::getStatus,status);

        dishService.update(updateWrapper);

        return R.success("状态修改成功");
    }
}
