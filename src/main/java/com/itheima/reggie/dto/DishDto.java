package com.itheima.reggie.dto;

import com.itheima.reggie.entity.Dish;
import com.itheima.reggie.entity.DishFlavor;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import java.util.ArrayList;
import java.util.List;

@Data
@ApiModel("菜品Dto")
public class DishDto extends Dish {

    //菜品对应的口味数据
    @ApiModelProperty("菜品对应的口味数据")
    private List<DishFlavor> flavors = new ArrayList<>();

    @ApiModelProperty("分类名称")
    private String categoryName;

    @ApiModelProperty("份数")
    private Integer copies;
}
