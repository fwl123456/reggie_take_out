package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.itheima.reggie.common.BaseContext;
import com.itheima.reggie.common.R;
import com.itheima.reggie.entity.AddressBook;
import com.itheima.reggie.service.AddressBookService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/addressBook")
public class AddressBookController {

    @Autowired
    private AddressBookService addressBookService;

    /**
     * 新增
     */
    @RequestMapping
    public R<AddressBook> save(@RequestBody AddressBook addressBook){
        addressBook.setUserId(BaseContext.getCurrentId());
        log.info("addressBook: {}", addressBook);
        addressBookService.save(addressBook);
        return R.success(addressBook);
    }

    // 设置默认地址
    @PutMapping("default")
    public R<AddressBook> setDefault(@RequestBody AddressBook addressBook){
        log.info("addressBook: {}", addressBook);
        // 条件构造器
        LambdaUpdateWrapper<AddressBook> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(AddressBook::getUserId, BaseContext.getCurrentId());
        wrapper.set(AddressBook::getIsDefault, 0);
        // 先将当前用户下面所有地址信息 is_default字段 都改为 0
        // sql: update address_book set is_default = 0 where user_id = ?
        addressBookService.update(wrapper);

        // 再把当前我们操作的这条数据is_default字段 改为1 默认地址
        addressBook.setIsDefault(1);
        // sql: update address_book set is_default = 1 where id = ?
        addressBookService.updateById(addressBook);
        return R.success(addressBook);
    }

    // 根据id查询地址
    @GetMapping("/{id}")
    public R get(@PathVariable Long id){
        AddressBook addressBook = addressBookService.getById(id);
        if(addressBook != null){
            return R.success(addressBook);
        }else {
            return R.error("没有找到该对象");
        }
    }

    // 查询默认地址
    @GetMapping("/default")
    public R<AddressBook> getDefault(){
        // 条件构造器
        LambdaQueryWrapper<AddressBook> queryWrapper = new LambdaQueryWrapper<>();
        // 先传入当前用户id
        queryWrapper.eq(AddressBook::getUserId, BaseContext.getCurrentId());
        queryWrapper.eq(AddressBook::getIsDefault, 1);
        //sql:select * from address_book where user_id = ? and is_default = 1
        AddressBook addressBook = addressBookService.getOne(queryWrapper);
        if(addressBook != null){
            return R.success(addressBook);
        }else {
            return R.error("没有找到该对象");
        }
    }


    // 查询指定用户的全部地址
    @GetMapping("/list")
    public R<List<AddressBook>> list(AddressBook addressBook){
        addressBook.setUserId(BaseContext.getCurrentId());
        log.info("addressBook: {}", addressBook);
        // 条件构造器
        LambdaQueryWrapper<AddressBook> queryWrapper = new LambdaQueryWrapper<>();
        // 下面的意思是如果当前操作的用户id不为空,才有这个查询条件 where user_id = ?
        queryWrapper.eq(addressBook.getUserId() != null, AddressBook::getUserId, addressBook.getUserId());
        // 根据更新时间倒序
        queryWrapper.orderByDesc(AddressBook::getUpdateTime);
        // sql: select * from address_book where user_id = ? order by update_time desc
        List<AddressBook> list = addressBookService.list(queryWrapper);
        return R.success(list);
    }

}
