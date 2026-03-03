package com.commerce_pro_backend.inventory.repository;

import com.commerce_pro_backend.inventory.entity.Warehouse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Warehouse Repository
 */
@Repository
public interface WarehouseRepository extends JpaRepository<Warehouse, String> {

    List<Warehouse> findByIsActiveTrue();

    Optional<Warehouse> findByIsDefaultTrue();

    boolean existsByCode(String code);

    @Query("SELECT w FROM Warehouse w WHERE w.isActive = true AND w.id NOT IN " +
           "(SELECT i.warehouse.id FROM Inventory i WHERE i.product.id = :productId)")
    List<Warehouse> findWarehousesWithoutProduct(String productId);
}
