package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.common.BaseContext;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.Orders;
import com.itheima.reggie.dto.OrdersDto;
import com.itheima.reggie.entity.User;
import com.itheima.reggie.service.OrderService;
import com.itheima.reggie.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.server.Session;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 订单
 */
@Slf4j
@RestController
@RequestMapping("/order")
@Api(tags = "订单接口")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private UserService userService;

    /**
     * 用户下单
     * @param orders
     * @return
     */
    @PostMapping("/submit")
    @CacheEvict(value = "orderCache",allEntries = true)
    @ApiOperation(value = "用户下单")
    public R<String> submit(@RequestBody Orders orders){
        log.info("订单数据：{}",orders);
        orderService.sumbit(orders);
        return R.success("下单成功");
    }

    /**
     * 订单信息分页查询
     * @param page
     * @param pageSize
     * @param number
     * @param beginTime
     * @param endTime
     * @return
     */
    @GetMapping("/page")
    @Cacheable(value = "orderCache",key = "'page' + #page + '_' + #pageSize + '_' + #number + '_' + #beginTime + '_' + #endTime")
    @ApiOperation(value = "订单信息分页查询")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "page",value = "页码",required = true),
            @ApiImplicitParam(name = "pageSize",value = "每页记录数",required = true),
            @ApiImplicitParam(name = "number",value = "订单号",required = false),
            @ApiImplicitParam(name = "beginTime",value = "开始时间",required = false),
            @ApiImplicitParam(name = "endTime",value = "结束时间",required = false)
    })
    public R<Page> page(int page, int pageSize,String number,String beginTime,String endTime){

        //构造分页构造器对象
        Page<Orders> ordersPage = new Page<>(page, pageSize);
        Page<OrdersDto> ordersDtoPage = new Page<>(page, pageSize);

        //条件构造器
        LambdaQueryWrapper<Orders> queryWrapper = new LambdaQueryWrapper<>();
        //添加过滤条件
        queryWrapper.like(number != null,Orders::getNumber,number);
        queryWrapper.ge(beginTime != null,Orders::getOrderTime,beginTime);
        queryWrapper.le(endTime != null,Orders::getOrderTime,endTime);

        orderService.page(ordersPage,queryWrapper);

        //对象拷贝,除了数据的集合(records)
        BeanUtils.copyProperties(ordersPage,ordersDtoPage,"records");
        //拿出数据集合
        List<Orders> records = ordersPage.getRecords();
        //数据循环

        List<OrdersDto> ordersDtos = records.stream().map((item) -> {
            OrdersDto ordersDto = new OrdersDto();
            //item的基础属性拷贝给dto
            BeanUtils.copyProperties(item,ordersDto);

            //查找用户最新的名称
            User user = userService.getById(item.getUserId());
            ordersDto.setUserName(user.getName());
            return ordersDto;
        }).collect(Collectors.toList());

        ordersDtoPage.setRecords(ordersDtos);

        return R.success(ordersDtoPage);
    }

    /**
     * 修改订单状态
     * @param orders
     * @return
     */
    @PutMapping
    @CacheEvict(value = "orderCache",allEntries = true)
    @ApiOperation(value = "修改订单状态")
    public R<String> update(@RequestBody Orders orders){
        log.info("orders:{}",orders);
        orderService.updateById(orders);
        return R.success("修改成功");
    }

    /**
     * 用户订单信息分页查询
     * @param page
     * @param pageSize
     * @return
     */
    @GetMapping("/userPage")
    @Cacheable(value = "orderCache",key = "'userPage' + #page + '_' + #pageSize")
    @ApiOperation(value = "用户订单信息分页查询")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "page",value = "页码",required = true),
            @ApiImplicitParam(name = "pageSize",value = "每页记录数",required = true),
    })
    public R<Page> userPage(int page, int pageSize){

        //分页构造器
        Page<Orders> ordersPage = new Page<>(page,pageSize);

        //条件构造器
        LambdaQueryWrapper<Orders> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Orders::getUserId, BaseContext.getCurrentId());

        orderService.page(ordersPage,queryWrapper);

        return R.success(ordersPage);
    }
}
