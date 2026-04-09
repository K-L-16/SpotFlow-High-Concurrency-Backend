package com.kl.repository;

import com.kl.entity.ShopType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ShopTypeRepository extends JpaRepository<ShopType, Long> {

    List<ShopType> findAllByOrderBySortAsc();
}
