package com.commerce_pro_backend.common.data;

import com.commerce_pro_backend.inventory.dto.InventoryRequestDTO;
import com.commerce_pro_backend.inventory.dto.WarehouseRequestDTO;
import com.commerce_pro_backend.inventory.entity.Warehouse;
import com.commerce_pro_backend.inventory.service.InventoryService;
import com.commerce_pro_backend.product.dto.ProductAttributeDTO;
import com.commerce_pro_backend.product.dto.ProductRequestDTO;
import com.commerce_pro_backend.product.dto.ProductResponseDTO;
import com.commerce_pro_backend.product.dto.ProductVariantDTO;
import com.commerce_pro_backend.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Data initializer for development environment
 * Creates sample products on startup with Unsplash images
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class DataInitializer {

    private String defaultWarehouseId;

    @Bean
    @Profile("dev")
    CommandLineRunner initData(ProductService productService, InventoryService inventoryService) {
        return args -> {
            log.info("Initializing sample data with Unsplash images...");

            // NOTE: To always re-create data on startup:
            // Option 1: Set spring.jpa.hibernate.ddl-auto=create-drop in application.properties
            // Option 2: Uncomment the lines below to clear existing data first
            
            // Uncomment to clear existing data before inserting new data:
            try {
                productService.getAllProducts().forEach(p -> productService.deleteProduct(p.getId()));
                log.info("Cleared existing products");
            } catch (Exception e) {
                log.warn("Could not clear existing products: {}", e.getMessage());
            }

            // Sample products with Unsplash images
            List<ProductRequestDTO> sampleProducts = List.of(
                    // Electronics
                    createProductWithVariants(
                            "Wireless Bluetooth Headphones Pro",
                            "ELEC-HP-001",
                            "Electronics",
                            "Sony",
                            new BigDecimal("199.99"),
                            new BigDecimal("249.99"),
                            150,
                            "https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=800&h=800&fit=crop",
                            List.of(
                                    "https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=800&h=800&fit=crop",
                                    "https://images.unsplash.com/photo-1484704849700-f032a568e944?w=800&h=800&fit=crop"
                            ),
                            "Premium noise-cancelling wireless headphones with 30-hour battery life and superior sound quality.",
                            List.of("electronics", "audio", "wireless", "headphones", "premium"),
                            // Attributes: Color
                            List.of(
                                    ProductAttributeDTO.builder().name("Color").values(List.of("Black", "Silver", "Blue")).displayOrder(1).build()
                            ),
                            // Variants
                            List.of(
                                    ProductVariantDTO.builder().name("Color").options(List.of("Black", "Silver", "Blue")).build()
                            )
                    ),
                    createProduct(
                            "Smart Watch Series 8",
                            "ELEC-SW-002",
                            "Electronics",
                            "TechBrand",
                            new BigDecimal("399.99"),
                            new BigDecimal("499.99"),
                            75,
                            "https://images.unsplash.com/photo-1523275335684-37898b6baf30?w=800&h=800&fit=crop",
                            List.of(
                                    "https://images.unsplash.com/photo-1523275335684-37898b6baf30?w=800&h=800&fit=crop",
                                    "https://images.unsplash.com/photo-1579586337278-3befd40fd17a?w=800&h=800&fit=crop"
                            ),
                            "Advanced fitness tracking, heart rate monitoring, and seamless smartphone integration.",
                            List.of("electronics", "wearable", "smartwatch", "fitness", "tech")
                    ),
                    createProduct(
                            "Mechanical Gaming Keyboard RGB",
                            "ELEC-KB-003",
                            "Electronics",
                            "KeyChamp",
                            new BigDecimal("149.99"),
                            new BigDecimal("199.99"),
                            60,
                            "https://images.unsplash.com/photo-1511467687858-23d96c32e4ae?w=800&h=800&fit=crop",
                            List.of(
                                    "https://images.unsplash.com/photo-1511467687858-23d96c32e4ae?w=800&h=800&fit=crop",
                                    "https://images.unsplash.com/photo-1595225476474-87563907a212?w=800&h=800&fit=crop"
                            ),
                            "Cherry MX switches, customizable RGB lighting, and aircraft-grade aluminum frame.",
                            List.of("electronics", "gaming", "keyboard", "mechanical", "rgb")
                    ),
                    createProduct(
                            "4K Ultra HD Webcam",
                            "ELEC-WC-004",
                            "Electronics",
                            "LogiTech",
                            new BigDecimal("129.99"),
                            new BigDecimal("169.99"),
                            200,
                            "https://images.unsplash.com/photo-1587829741301-dc798b83add3?w=800&h=800&fit=crop",
                            List.of(
                                    "https://images.unsplash.com/photo-1587829741301-dc798b83add3?w=800&h=800&fit=crop"
                            ),
                            "Crystal clear 4K video with auto-focus and noise-reducing microphones.",
                            List.of("electronics", "webcam", "streaming", "4k", "video")
                    ),
                    createProduct(
                            "Portable Bluetooth Speaker",
                            "ELEC-SP-005",
                            "Electronics",
                            "BoseAudio",
                            new BigDecimal("79.99"),
                            new BigDecimal("99.99"),
                            180,
                            "https://images.unsplash.com/photo-1608043152269-423dbba4e7e1?w=800&h=800&fit=crop",
                            List.of(
                                    "https://images.unsplash.com/photo-1608043152269-423dbba4e7e1?w=800&h=800&fit=crop",
                                    "https://images.unsplash.com/photo-1545454675-3531b543be5d?w=800&h=800&fit=crop"
                            ),
                            "Waterproof portable speaker with 360-degree sound and 12-hour battery.",
                            List.of("electronics", "speaker", "bluetooth", "portable", "audio")
                    ),

                    // Fashion
                    createProduct(
                            "Classic Leather Backpack",
                            "FASH-BP-006",
                            "Fashion",
                            "Heritage Leather",
                            new BigDecimal("149.99"),
                            new BigDecimal("199.99"),
                            85,
                            "https://images.unsplash.com/photo-1553062407-98eeb64c6a62?w=800&h=800&fit=crop",
                            List.of(
                                    "https://images.unsplash.com/photo-1553062407-98eeb64c6a62?w=800&h=800&fit=crop",
                                    "https://images.unsplash.com/photo-1622560480605-d83c853bc5c3?w=800&h=800&fit=crop"
                            ),
                            "Handcrafted genuine leather backpack with laptop compartment and multiple pockets.",
                            List.of("fashion", "bags", "leather", "backpack", "accessories")
                    ),
                    createProduct(
                            "Premium Sunglasses UV400",
                            "FASH-SG-007",
                            "Fashion",
                            "RayStyle",
                            new BigDecimal("89.99"),
                            new BigDecimal("129.99"),
                            120,
                            "https://images.unsplash.com/photo-1572635196237-14b3f281503f?w=800&h=800&fit=crop",
                            List.of(
                                    "https://images.unsplash.com/photo-1572635196237-14b3f281503f?w=800&h=800&fit=crop",
                                    "https://images.unsplash.com/photo-1511499767150-a48a237f0083?w=800&h=800&fit=crop"
                            ),
                            "Polarized lenses with 100% UV protection in a timeless aviator design.",
                            List.of("fashion", "sunglasses", "accessories", "uv-protection", "premium")
                    ),
                    createProduct(
                            "Minimalist Leather Wallet",
                            "FASH-WL-008",
                            "Fashion",
                            "LeatherCraft",
                            new BigDecimal("49.99"),
                            new BigDecimal("69.99"),
                            250,
                            "https://images.unsplash.com/photo-1627123424574-724758594e93?w=800&h=800&fit=crop",
                            List.of(
                                    "https://images.unsplash.com/photo-1627123424574-724758594e93?w=800&h=800&fit=crop"
                            ),
                            "Slim bifold design with RFID blocking technology and premium full-grain leather.",
                            List.of("fashion", "wallet", "leather", "accessories", "minimalist")
                    ),
                    createProductWithVariants(
                            "Canvas Sneakers Classic",
                            "FASH-SN-009",
                            "Fashion",
                            "StreetWear",
                            new BigDecimal("59.99"),
                            new BigDecimal("79.99"),
                            300,
                            "https://images.unsplash.com/photo-1600269452121-4f2416e55c28?w=800&h=800&fit=crop",
                            List.of(
                                    "https://images.unsplash.com/photo-1600269452121-4f2416e55c28?w=800&h=800&fit=crop",
                                    "https://images.unsplash.com/photo-1606107557195-0e29a4b5b4aa?w=800&h=800&fit=crop"
                            ),
                            "Iconic canvas sneakers with vulcanized rubber sole and cushioned insole.",
                            List.of("fashion", "shoes", "sneakers", "casual", "canvas"),
                            // Attributes: Size and Color
                            List.of(
                                    ProductAttributeDTO.builder().name("Size").values(List.of("7", "8", "9", "10", "11")).displayOrder(1).build(),
                                    ProductAttributeDTO.builder().name("Color").values(List.of("White", "Black", "Red", "Navy")).displayOrder(2).build()
                            ),
                            // Variants based on attributes
                            List.of(
                                    ProductVariantDTO.builder().name("Size").options(List.of("7", "8", "9", "10", "11")).build(),
                                    ProductVariantDTO.builder().name("Color").options(List.of("White", "Black", "Red", "Navy")).build()
                            )
                    ),
                    createProduct(
                            "Designer Watch Automatic",
                            "FASH-WT-010",
                            "Fashion",
                            "TimePiece",
                            new BigDecimal("299.99"),
                            new BigDecimal("399.99"),
                            45,
                            "https://images.unsplash.com/photo-1524592094714-0f0654e20314?w=800&h=800&fit=crop",
                            List.of(
                                    "https://images.unsplash.com/photo-1524592094714-0f0654e20314?w=800&h=800&fit=crop",
                                    "https://images.unsplash.com/photo-1522312346375-d1a52e2b99b3?w=800&h=800&fit=crop"
                            ),
                            "Self-winding automatic movement with sapphire crystal and stainless steel case.",
                            List.of("fashion", "watch", "luxury", "automatic", "accessories")
                    ),

                    // Home & Kitchen
                    createProduct(
                            "Pour-Over Coffee Maker Set",
                            "HOME-CM-011",
                            "Home & Kitchen",
                            "BrewMaster",
                            new BigDecimal("59.99"),
                            new BigDecimal("79.99"),
                            95,
                            "https://images.unsplash.com/photo-1544787219-7f47ccb76574?w=800&h=800&fit=crop",
                            List.of(
                                    "https://images.unsplash.com/photo-1544787219-7f47ccb76574?w=800&h=800&fit=crop",
                                    "https://images.unsplash.com/photo-1495474472287-4d71bcdd2085?w=800&h=800&fit=crop"
                            ),
                            "Complete pour-over set with glass carafe, ceramic dripper, and filter papers.",
                            List.of("home", "kitchen", "coffee", "brewing", "pour-over")
                    ),
                    createProduct(
                            "Stainless Steel Water Bottle",
                            "HOME-WB-012",
                            "Home & Kitchen",
                            "HydroLife",
                            new BigDecimal("34.99"),
                            new BigDecimal("44.99"),
                            400,
                            "https://images.unsplash.com/photo-1602143407151-7111542de6e8?w=800&h=800&fit=crop",
                            List.of(
                                    "https://images.unsplash.com/photo-1602143407151-7111542de6e8?w=800&h=800&fit=crop"
                            ),
                            "Double-wall vacuum insulated bottle keeps drinks cold for 24 hours or hot for 12.",
                            List.of("home", "kitchen", "water-bottle", "eco-friendly", "insulated")
                    ),
                    createProduct(
                            "Ceramic Dinnerware Set",
                            "HOME-DW-013",
                            "Home & Kitchen",
                            "ArtisanHome",
                            new BigDecimal("129.99"),
                            new BigDecimal("169.99"),
                            60,
                            "https://images.unsplash.com/photo-1610701596007-11502861dcfa?w=800&h=800&fit=crop",
                            List.of(
                                    "https://images.unsplash.com/photo-1610701596007-11502861dcfa?w=800&h=800&fit=crop",
                                    "https://images.unsplash.com/photo-1578749556568-bc2c40e68b61?w=800&h=800&fit=crop"
                            ),
                            "Handcrafted ceramic dinnerware set for 4 with minimalist modern design.",
                            List.of("home", "kitchen", "dinnerware", "ceramic", "dining")
                    ),
                    createProduct(
                            "Smart Air Purifier",
                            "HOME-AP-014",
                            "Home & Kitchen",
                            "PureAir",
                            new BigDecimal("199.99"),
                            new BigDecimal("249.99"),
                            40,
                            "https://images.unsplash.com/photo-1585771724684-38269d6639fd?w=800&h=800&fit=crop",
                            List.of(
                                    "https://images.unsplash.com/photo-1585771724684-38269d6639fd?w=800&h=800&fit=crop"
                            ),
                            "HEPA filtration with app control, air quality monitoring, and quiet operation.",
                            List.of("home", "appliances", "air-purifier", "smart-home", "health")
                    ),

                    // Sports
                    createProduct(
                            "Yoga Mat Premium Non-Slip",
                            "SPRT-YM-015",
                            "Sports",
                            "FitGear",
                            new BigDecimal("45.99"),
                            new BigDecimal("59.99"),
                            150,
                            "https://images.unsplash.com/photo-1601925260368-ae2f83cf8b7f?w=800&h=800&fit=crop",
                            List.of(
                                    "https://images.unsplash.com/photo-1601925260368-ae2f83cf8b7f?w=800&h=800&fit=crop",
                                    "https://images.unsplash.com/photo-1544367567-0f2fcb009e0b?w=800&h=800&fit=crop"
                            ),
                            "Eco-friendly TPE material with alignment lines and carrying strap.",
                            List.of("sports", "fitness", "yoga", "mat", "exercise")
                    ),
                    createProduct(
                            "Adjustable Dumbbells Set",
                            "SPRT-DB-016",
                            "Sports",
                            "PowerFit",
                            new BigDecimal("299.99"),
                            new BigDecimal("399.99"),
                            35,
                            "https://images.unsplash.com/photo-1638536532686-d610adfc8e5c?w=800&h=800&fit=crop",
                            List.of(
                                    "https://images.unsplash.com/photo-1638536532686-d610adfc8e5c?w=800&h=800&fit=crop"
                            ),
                            "Adjustable from 5 to 52.5 lbs with easy dial system. Space-saving design.",
                            List.of("sports", "fitness", "dumbbells", "strength", "home-gym")
                    ),
                    createProductWithVariants(
                            "Running Shoes Performance",
                            "SPRT-RS-017",
                            "Sports",
                            "NikeRun",
                            new BigDecimal("129.99"),
                            new BigDecimal("159.99"),
                            200,
                            "https://images.unsplash.com/photo-1542291026-7eec264c27ff?w=800&h=800&fit=crop",
                            List.of(
                                    "https://images.unsplash.com/photo-1542291026-7eec264c27ff?w=800&h=800&fit=crop",
                                    "https://images.unsplash.com/photo-1606107557195-0e29a4b5b4aa?w=800&h=800&fit=crop"
                            ),
                            "Responsive cushioning and breathable mesh upper for maximum comfort.",
                            List.of("sports", "running", "shoes", "athletic", "performance"),
                            // Attributes: Size and Color
                            List.of(
                                    ProductAttributeDTO.builder().name("Size").values(List.of("7", "8", "9", "10", "11", "12")).displayOrder(1).build(),
                                    ProductAttributeDTO.builder().name("Color").values(List.of("Black/Red", "White/Blue", "Grey/Neon")).displayOrder(2).build()
                            ),
                            // Variants
                            List.of(
                                    ProductVariantDTO.builder().name("Size").options(List.of("7", "8", "9", "10", "11", "12")).build(),
                                    ProductVariantDTO.builder().name("Color").options(List.of("Black/Red", "White/Blue", "Grey/Neon")).build()
                            )
                    ),
                    createProduct(
                            "Resistance Bands Set",
                            "SPRT-RB-018",
                            "Sports",
                            "FlexFit",
                            new BigDecimal("24.99"),
                            new BigDecimal("34.99"),
                            500,
                            "https://images.unsplash.com/photo-1598289431512-b97b0917affc?w=800&h=800&fit=crop",
                            List.of(
                                    "https://images.unsplash.com/photo-1598289431512-b97b0917affc?w=800&h=800&fit=crop"
                            ),
                            "5 levels of resistance with handles, ankle straps, and door anchor.",
                            List.of("sports", "fitness", "resistance-bands", "training", "home-workout")
                    ),

                    // Books & Media
                    createProduct(
                            "Professional Camera DSLR",
                            "ELEC-CM-019",
                            "Electronics",
                            "CanonPro",
                            new BigDecimal("899.99"),
                            new BigDecimal("1099.99"),
                            25,
                            "https://images.unsplash.com/photo-1516035069371-29a1b244cc32?w=800&h=800&fit=crop",
                            List.of(
                                    "https://images.unsplash.com/photo-1516035069371-29a1b244cc32?w=800&h=800&fit=crop",
                                    "https://images.unsplash.com/photo-1502920917128-1aa500764cbd?w=800&h=800&fit=crop"
                            ),
                            "24.1MP full-frame sensor, 4K video, and advanced autofocus system.",
                            List.of("electronics", "camera", "photography", "dslr", "professional")
                    ),
                    createProduct(
                            "Wireless Earbuds Pro",
                            "ELEC-EB-020",
                            "Electronics",
                            "SoundTech",
                            new BigDecimal("149.99"),
                            new BigDecimal("199.99"),
                            300,
                            "https://images.unsplash.com/photo-1590658268037-6bf12165a8df?w=800&h=800&fit=crop",
                            List.of(
                                    "https://images.unsplash.com/photo-1590658268037-6bf12165a8df?w=800&h=800&fit=crop",
                                    "https://images.unsplash.com/photo-1606220588913-b3aacb4d2f46?w=800&h=800&fit=crop"
                            ),
                            "Active noise cancellation, transparency mode, and 24-hour battery with case.",
                            List.of("electronics", "audio", "earbuds", "wireless", "noise-cancelling")
                    ),

                    // Additional products for variety
                    createProduct(
                            "Smart Home Hub",
                            "ELEC-SH-021",
                            "Electronics",
                            "SmartLife",
                            new BigDecimal("79.99"),
                            new BigDecimal("99.99"),
                            80,
                            "https://images.unsplash.com/photo-1558089687-f282ffcbc126?w=800&h=800&fit=crop",
                            List.of(
                                    "https://images.unsplash.com/photo-1558089687-f282ffcbc126?w=800&h=800&fit=crop"
                            ),
                            "Control all your smart devices with voice commands and automation.",
                            List.of("electronics", "smart-home", "hub", "automation", "iot")
                    ),
                    createProduct(
                            "Ergonomic Office Chair",
                            "HOME-OC-022",
                            "Home & Kitchen",
                            "ComfortSeating",
                            new BigDecimal("349.99"),
                            new BigDecimal("449.99"),
                            30,
                            "https://images.unsplash.com/photo-1505843490538-5133c6c7d0e1?w=800&h=800&fit=crop",
                            List.of(
                                    "https://images.unsplash.com/photo-1505843490538-5133c6c7d0e1?w=800&h=800&fit=crop"
                            ),
                            "Adjustable lumbar support, breathable mesh, and 4D armrests.",
                            List.of("home", "furniture", "office", "chair", "ergonomic")
                    )
            );

            int created = 0;
            List<ProductResponseDTO> createdProducts = new ArrayList<>();
            for (ProductRequestDTO product : sampleProducts) {
                try {
                    ProductResponseDTO createdProduct = productService.createProduct(product);
                    createdProducts.add(createdProduct);
                    created++;
                    log.info("Created product [{}]: {}", created, product.getName());
                } catch (Exception e) {
                    log.error("Failed to create product: {}", product.getName(), e);
                }
            }

            log.info("Sample data initialization completed. Created {} products.", created);

            // Initialize Inventory Data
            if (created > 0) {
                initInventoryData(inventoryService, createdProducts);
            }
        };
    }

    private void initInventoryData(InventoryService inventoryService, List<ProductResponseDTO> products) {
        log.info("Initializing inventory data...");

        try {
            // Create default warehouse
            WarehouseRequestDTO mainWarehouse = WarehouseRequestDTO.builder()
                    .name("Main Warehouse")
                    .code("MAIN-001")
                    .address("123 Commerce Street, Industrial District")
                    .city("New York")
                    .state("NY")
                    .country("USA")
                    .postalCode("10001")
                    .managerName("John Smith")
                    .managerEmail("john.smith@commercepro.com")
                    .managerPhone("+1-555-0100")
                    .isActive(true)
                    .isDefault(true)
                    .build();

            var warehouse = inventoryService.createWarehouse(mainWarehouse);
            defaultWarehouseId = warehouse.getId();
            log.info("Created default warehouse: {}", warehouse.getId());

            // Create secondary warehouse
            WarehouseRequestDTO secondaryWarehouse = WarehouseRequestDTO.builder()
                    .name("West Coast Warehouse")
                    .code("WEST-002")
                    .address("456 Logistics Blvd, Port Area")
                    .city("Los Angeles")
                    .state("CA")
                    .country("USA")
                    .postalCode("90001")
                    .managerName("Sarah Johnson")
                    .managerEmail("sarah.johnson@commercepro.com")
                    .managerPhone("+1-555-0200")
                    .isActive(true)
                    .isDefault(false)
                    .build();

            var warehouse2 = inventoryService.createWarehouse(secondaryWarehouse);
            log.info("Created secondary warehouse: {}", warehouse2.getId());

            // Create inventory records for each product
            int inventoryCount = 0;
            String[] zones = {"A", "B", "C", "D"};
            String[] aisles = {"01", "02", "03", "04", "05"};

            for (int i = 0; i < products.size(); i++) {
                ProductResponseDTO product = products.get(i);
                try {
                    // Determine stock level based on index to create variety
                    int baseStock = product.getStock();
                    
                    // Create inventory in main warehouse (70-100% of stock)
                    int mainStock = (int) (baseStock * (0.7 + Math.random() * 0.3));
                    
                    InventoryRequestDTO inventory1 = InventoryRequestDTO.builder()
                            .productId(product.getId())
                            .warehouseId(defaultWarehouseId)
                            .quantity(mainStock)
                            .reserved((int) (mainStock * 0.1)) // 10% reserved
                            .lowStockThreshold(Math.max(5, (int) (baseStock * 0.15)))
                            .reorderPoint(Math.max(10, (int) (baseStock * 0.25)))
                            .reorderQuantity((int) (baseStock * 0.5))
                            .unitCost(product.getCost() != null ? product.getCost() : product.getPrice().multiply(new BigDecimal("0.6")))
                            .binLocation(zones[i % 4] + "-" + aisles[i % 5] + "-" + String.format("%03d", i + 1))
                            .zone(zones[i % 4])
                            .aisle(aisles[i % 5])
                            .trackInventory(true)
                            .build();

                    inventoryService.createInventory(inventory1);

                    // Create inventory in secondary warehouse for some products (20-30% of stock)
                    if (i % 3 == 0) {
                        int secondaryStock = (int) (baseStock * (0.2 + Math.random() * 0.1));
                        
                        InventoryRequestDTO inventory2 = InventoryRequestDTO.builder()
                                .productId(product.getId())
                                .warehouseId(warehouse2.getId())
                                .quantity(secondaryStock)
                                .reserved(0)
                                .lowStockThreshold(Math.max(5, (int) (baseStock * 0.15)))
                                .reorderPoint(Math.max(10, (int) (baseStock * 0.25)))
                                .reorderQuantity((int) (baseStock * 0.5))
                                .unitCost(product.getCost() != null ? product.getCost() : product.getPrice().multiply(new BigDecimal("0.6")))
                                .binLocation(zones[(i + 2) % 4] + "-" + aisles[(i + 2) % 5] + "-" + String.format("%03d", i + 1))
                                .zone(zones[(i + 2) % 4])
                                .aisle(aisles[(i + 2) % 5])
                                .trackInventory(true)
                                .build();

                        inventoryService.createInventory(inventory2);
                    }

                    inventoryCount++;
                } catch (Exception e) {
                    log.error("Failed to create inventory for product: {}", product.getName(), e);
                }
            }

            log.info("Inventory data initialization completed. Created inventory for {} products.", inventoryCount);

            // Log inventory stats
            var stats = inventoryService.getInventoryStats();
            log.info("Inventory Stats - Total Items: {}, Total Value: ${}", 
                    stats.getTotalItems(), stats.getTotalInventoryValue());
            log.info("Stock Status - In Stock: {}, Low Stock: {}, Out of Stock: {}",
                    stats.getInStockCount(), stats.getLowStockCount(), stats.getOutOfStockCount());

        } catch (Exception e) {
            log.error("Failed to initialize inventory data", e);
        }
    }

    private ProductRequestDTO createProduct(
            String name,
            String sku,
            String category,
            String brand,
            BigDecimal price,
            BigDecimal comparePrice,
            int stock,
            String image,
            List<String> gallery,
            String description,
            List<String> tags) {
        
        return createProductWithVariants(name, sku, category, brand, price, comparePrice, stock, image, gallery, description, tags, null, null);
    }

    private ProductRequestDTO createProductWithVariants(
            String name,
            String sku,
            String category,
            String brand,
            BigDecimal price,
            BigDecimal comparePrice,
            int stock,
            String image,
            List<String> gallery,
            String description,
            List<String> tags,
            List<ProductAttributeDTO> attributes,
            List<ProductVariantDTO> variants) {
        
        return ProductRequestDTO.builder()
                .name(name)
                .sku(sku)
                .description(description)
                .shortDescription(description.substring(0, Math.min(100, description.length())) + "...")
                .category(category)
                .brand(brand)
                .price(price)
                .compareAtPrice(comparePrice)
                .cost(price.multiply(new BigDecimal("0.6"))) // 40% margin
                .stock(stock)
                .lowStockThreshold(10)
                .status("active")
                .visibility("visible")
                .image(image)
                .gallery(gallery)
                .tags(tags)
                .featured(Math.random() > 0.7) // 30% featured
                .trackInventory(true)
                .allowBackorders(false)
                .vendor(brand)
                .productType("Physical")
                .attributes(attributes != null ? attributes : new ArrayList<>())
                .variants(variants != null ? variants : new ArrayList<>())
                .build();
    }
}
