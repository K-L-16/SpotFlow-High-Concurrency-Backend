package com.kl.repository;

import com.kl.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * User Repository
 * 第一个参数表示操作的实体类
 * 第二个参数表示实体类中被@id注解修饰的属性数据类型（主键的数据类型）
 */
public interface UserRepository extends JpaRepository<User,Long> {

    /**
     * 根据手机号查询用户
     * @param phone
     * @return
     */
    Optional<User> findByPhone(String phone);



}
