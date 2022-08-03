package com.itheima.reggie.dto;

import com.itheima.reggie.entity.OrderDetail;
import com.itheima.reggie.entity.Orders;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.util.List;

@Data
@ApiModel("订单Dto")
public class OrdersDto extends Orders {

    @ApiModelProperty("用户名")
    private String userName;

//    private String phone;
//
//    private String address;
//
//    private String consignee;

    @ApiModelProperty("订单明细")
    private List<OrderDetail> orderDetails;
	
}
