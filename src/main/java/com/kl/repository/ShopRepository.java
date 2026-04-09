package com.kl.repository;

import com.kl.entity.Shop;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShopRepository extends JpaRepository<Shop, Long> {

    Page<Shop> findByNameContaining(String name, Pageable pageable);

    Page<Shop> findByTypeId(Long typeId, Pageable pageable);
}
