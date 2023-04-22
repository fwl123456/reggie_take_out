package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.itheima.reggie.common.BaseContext;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.ShoppingCart;
import com.itheima.reggie.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@Slf4j
@RequestMapping("/shoppingCart")
public class ShoppingCartController {

    @Autowired
    private ShoppingCartService shoppingCartService;


    /**
     * 添加购物车
     * @param shoppingCart
     * @return
     */
    @PostMapping("/add")
    public R<ShoppingCart> add(@RequestBody ShoppingCart shoppingCart){
        log.info("购物车数据: {}", shoppingCart);
        // 设置用户id,指定当前是哪个用户的购物车数据
        Long currentId = BaseContext.getCurrentId();
        shoppingCart.setUserId(currentId);

        // 查询当前菜品或套餐是否在购物车中
        Long dishId = shoppingCart.getDishId();

        // 判断购物车中是否已经存在菜品 通过用户id和菜品id查询
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        // 先通过用户id
        queryWrapper.eq(ShoppingCart::getUserId, currentId);
        if(dishId != null){
            // 添加到购物车的是菜品
            queryWrapper.eq(ShoppingCart::getDishId, dishId);
        }else {
            // 添加到购物车的是套餐
            queryWrapper.eq(ShoppingCart::getSetmealId, shoppingCart.getSetmealId());
        }
        // 查询套餐或菜品是否在购物车中存在
        // sql: select * from shopping_cart where user_id = ? and dish_id/setmeal_id =?
        ShoppingCart cartServiceOne = shoppingCartService.getOne(queryWrapper);
        if (cartServiceOne != null){
            // 如果已经存在,就在原来数量的基础上加1
            // 先获取原先数量
            Integer number = cartServiceOne.getNumber();
            // 在原先数量基础上+1
            cartServiceOne.setNumber(number + 1);
            // 更新数据
            shoppingCartService.updateById(cartServiceOne);
        }else {
            // 如果不存在,则添加到购物车,数量默认1
            // 新增购物车数据
            shoppingCart.setNumber(1);
            shoppingCart.setCreateTime(LocalDateTime.now());
            shoppingCartService.save(shoppingCart);
            // 没查询出来给cartServiceOne赋值要返回给前端 方便页面使用该数据
            cartServiceOne = shoppingCart;
        }
        return R.success(cartServiceOne);
    }

    /**
     * 查看购物车
     * @return
     */
    @GetMapping("/list")
    public R<List<ShoppingCart>> list(){
        log.info("查看购物车....");
        // 得到当前用户id
        Long currentId = BaseContext.getCurrentId();
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId, currentId);
        queryWrapper.orderByAsc(ShoppingCart::getCreateTime);
        List<ShoppingCart> list = shoppingCartService.list(queryWrapper);
        return R.success(list);
    }

    /**
     * 清空购物车
     * @return
     */
    @DeleteMapping("/clean")
    public R<String> clean(){
        // 得到当前用户id
        Long currentId = BaseContext.getCurrentId();
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId, currentId);
        // sql: delete from shopping_cart where user_id ?
        shoppingCartService.remove(queryWrapper);
        return R.success("清空购物车成功!");
    }


    /**
     * 移除购物车商品
     * @param shoppingCart
     * @return
     */
    @PostMapping("/sub")
    public R<String> sub(@RequestBody ShoppingCart shoppingCart){
        // 得到当前用户id
        Long currentId = BaseContext.getCurrentId();
        // 构造添加构造器
        LambdaQueryWrapper<ShoppingCart> queryWrapper = new LambdaQueryWrapper<>();
        // 先传递用户id查询条件
        queryWrapper.eq(ShoppingCart::getUserId, currentId);
        // 判断减少的是菜品还是套餐
        Long dishId = shoppingCart.getDishId();
        if(dishId != null){
            // 菜品id
            queryWrapper.eq(ShoppingCart::getDishId, dishId);
        }else {
            // 套餐id
            queryWrapper.eq(ShoppingCart::getSetmealId, shoppingCart.getSetmealId());
        }
        // 查询菜品或套餐
        ShoppingCart cartServiceOne = shoppingCartService.getOne(queryWrapper);
        // 判断查询返回的菜品或者套餐数量
        Integer number = cartServiceOne.getNumber();
        if(number > 1){
            // 数量大于1的话就吧当前菜品数量减1
            cartServiceOne.setNumber(number - 1);
            shoppingCartService.updateById(cartServiceOne);
            return R.success("菜品数量减少成功");
        }else {
            // 数量等于1直接删除
            shoppingCartService.remove(queryWrapper);
            return R.success("菜品删除成功");
        }
    }
}
