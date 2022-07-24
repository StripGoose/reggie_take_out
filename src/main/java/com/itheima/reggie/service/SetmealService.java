package com.itheima.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.reggie.dto.SetmealDto;
import com.itheima.reggie.entity.Setmeal;

import java.util.List;

public interface SetmealService extends IService<Setmeal> {

    /**
     * 新增套餐，同时需要保存套餐和菜品的关联关系
     * @param setmealDto
     */
    public void saveWithDish(SetmealDto setmealDto);

    /**
     * 删除套餐，同时需要删除套餐和菜品的关联数据
     * @param ids
     */
    public void removeWithDish(List<Long> ids);

    /**
     * 根据id查询套餐，同时需要查询相关菜品
     * @param id
     */
    public SetmealDto getByIdWithDish(Long id);

    /**
     * 根据id更新套餐，同时更新相关菜品
     * @param setmealDto
     * @return
     */
    public void updateWithDish(SetmealDto setmealDto);
}
