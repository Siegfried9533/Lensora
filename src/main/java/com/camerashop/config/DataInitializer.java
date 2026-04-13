package com.camerashop.config;

import com.camerashop.entity.*;
import com.camerashop.entity.User.Role;
import com.camerashop.entity.Category.EntityType;
import com.camerashop.entity.Asset.AssetStatus;
import com.camerashop.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private AssetRepository assetRepository;

    @Autowired
    private ProductImageRepository productImageRepository;

    @Autowired
    private AssetImageRepository assetImageRepository;

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) {
            return; // Data already exists
        }

        // Create Users
        User testUser = User.builder()
                .userName("testuser")
                .email("test@example.com")
                .password(passwordEncoder.encode("password123"))
                .role(Role.USER)
                .trustScore(85)
                .build();
        userRepository.save(testUser);

        User adminUser = User.builder()
                .userName("johndoe")
                .email("john@example.com")
                .password(passwordEncoder.encode("password123"))
                .role(Role.ADMIN)
                .trustScore(99)
                .build();
        userRepository.save(adminUser);

        // Create Categories
        List<Category> productCategories = Arrays.asList(
            Category.builder().categoryId("c1").categoryName("Premium Camera").type(EntityType.PRODUCT).build(),
            Category.builder().categoryId("c2").categoryName("Medium Format").type(EntityType.PRODUCT).build(),
            Category.builder().categoryId("c3").categoryName("Mirrorless").type(EntityType.PRODUCT).build(),
            Category.builder().categoryId("c7").categoryName("Action Camera").type(EntityType.PRODUCT).build(),
            Category.builder().categoryId("c8").categoryName("Drone").type(EntityType.PRODUCT).build(),
            Category.builder().categoryId("c9").categoryName("Accessories").type(EntityType.PRODUCT).build()
        );
        categoryRepository.saveAll(productCategories);
        categoryRepository.flush();

        List<Category> assetCategories = Arrays.asList(
            Category.builder().categoryId("c4").categoryName("Camera Body").type(EntityType.ASSET).build(),
            Category.builder().categoryId("c5").categoryName("Lens").type(EntityType.ASSET).build(),
            Category.builder().categoryId("c6").categoryName("Lighting").type(EntityType.ASSET).build(),
            Category.builder().categoryId("c10").categoryName("Stabilizer").type(EntityType.ASSET).build(),
            Category.builder().categoryId("c11").categoryName("Audio").type(EntityType.ASSET).build()
        );
        categoryRepository.saveAll(assetCategories);
        categoryRepository.flush();

        // Create Products
        List<Product> products = Arrays.asList(
            createProduct(testUser, productCategories.get(0), "LEICA M11", "Leica", "The newest digital rangefinder from Leica combining classic design with contemporary technology.", 199000000L, 5),
            createProduct(testUser, productCategories.get(1), "HASSELBLAD X2D", "Hasselblad", "100-megapixel medium format mirrorless camera for ultimate image quality.", 175000000L, 2),
            createProduct(testUser, productCategories.get(2), "SONY α1", "Sony", "The one. Flagship full-frame mirrorless camera with 50.1MP and 30fps shooting.", 145000000L, 10),
            createProduct(testUser, productCategories.get(2), "CANON EOS R3", "Canon", "High performance sports and wildlife mirrorless camera.", 135000000L, 4),
            createProduct(testUser, productCategories.get(2), "NIKON Z9", "Nikon", "Professional full-frame mirrorless without mechanical shutter.", 125000000L, 7),
            createProduct(testUser, productCategories.get(2), "FUJIFILM X-H2S", "Fujifilm", "High speed APS-C camera with stacked sensor technology.", 58000000L, 15),
            createProduct(testUser, productCategories.get(3), "GoPro HERO 12 Black", "GoPro", "The ultimate action camera for extreme sports.", 9500000L, 50),
            createProduct(testUser, productCategories.get(3), "DJI Osmo Action 4", "DJI", "Excellent low light action cam.", 8500000L, 45),
            createProduct(testUser, productCategories.get(4), "DJI Mavic 3 Pro", "DJI", "Triple camera drone for professional cinematic shots.", 52000000L, 8),
            createProduct(testUser, productCategories.get(4), "DJI Mini 4 Pro", "DJI", "Mini camera drone under 249g with omnidirectional obstacle sensing.", 23000000L, 20),
            createProduct(testUser, productCategories.get(5), "Peak Design Everyday Backpack", "Peak Design", "Award winning camera bags for everyday carry.", 6500000L, 30),
            createProduct(testUser, productCategories.get(5), "ProGrade CFexpress Type B 512GB", "ProGrade", "Ultra fast memory card for 8K video.", 12500000L, 12)
        );
        productRepository.saveAll(products);

        // Create Assets
        List<Asset> assets = Arrays.asList(
            createAsset(testUser, assetCategories.get(0), "Canon EOS R5", "Canon", 800000L, AssetStatus.AVAILABLE, "R5-001239"),
            createAsset(testUser, assetCategories.get(1), "Sony FE 24-70mm f/2.8 GM II", "Sony", 400000L, AssetStatus.AVAILABLE, "GM2-45211"),
            createAsset(testUser, assetCategories.get(0), "RED Komodo 6K", "RED", 2500000L, AssetStatus.RENTED, "KMD-99120"),
            createAsset(testUser, assetCategories.get(0), "ARRI Alexa Mini LF", "ARRI", 8000000L, AssetStatus.AVAILABLE, "ALX-10332"),
            createAsset(testUser, assetCategories.get(1), "Canon RF 70-200mm f/2.8", "Canon", 500000L, AssetStatus.AVAILABLE, "RF72-1200"),
            createAsset(testUser, assetCategories.get(1), "Sigma Art 35mm f/1.4", "Sigma", 200000L, AssetStatus.AVAILABLE, "SG-ART35-1"),
            createAsset(testUser, assetCategories.get(2), "Aputure LS 600d Pro", "Aputure", 700000L, AssetStatus.AVAILABLE, "AP-600D-PRO"),
            createAsset(testUser, assetCategories.get(2), "Profoto B10X Plus", "Profoto", 600000L, AssetStatus.RENTED, "PR-B10XP"),
            createAsset(testUser, assetCategories.get(3), "DJI RS 3 Pro Gimbal", "DJI", 450000L, AssetStatus.AVAILABLE, "RS3P-001"),
            createAsset(testUser, assetCategories.get(3), "Zhiyun Crane 3S", "Zhiyun", 350000L, AssetStatus.AVAILABLE, "ZY-C3S-88"),
            createAsset(testUser, assetCategories.get(4), "Sennheiser MKH 416", "Sennheiser", 300000L, AssetStatus.RENTED, "SN-416-09"),
            createAsset(testUser, assetCategories.get(4), "Rode Wireless GO II", "Rode", 150000L, AssetStatus.AVAILABLE, "RD-WG2-11")
        );
        assetRepository.saveAll(assets);

        // Create Images for Products
        List<String> photoPool = Arrays.asList(
            "https://images.unsplash.com/photo-1725779318629-eda3e096eb86?w=800",
            "https://images.unsplash.com/photo-1511140973288-19bf21d7e771?w=800",
            "https://images.unsplash.com/photo-1585548601784-e319505354bb?w=800",
            "https://images.unsplash.com/photo-1516035069371-29a1b244cc32?w=800",
            "https://images.unsplash.com/photo-1516961642265-531546e84af2?w=800",
            "https://images.unsplash.com/photo-1617005082833-18e8093153e7?w=800",
            "https://images.unsplash.com/photo-1502920917128-1aa500764cbd?w=800",
            "https://images.unsplash.com/photo-1560064278-65127ee6aa25?w=800",
            "https://images.unsplash.com/photo-1493770348161-369560ae357d?w=800",
            "https://images.unsplash.com/photo-1514316454349-750a7fd3da3a?w=800",
            "https://images.unsplash.com/photo-1452423924765-680fa2a9121a?w=800",
            "https://images.unsplash.com/photo-1520390138845-fd2d229dd553?w=800"
        );

        for (int i = 0; i < products.size(); i++) {
            Product product = products.get(i);
            ProductImage image = ProductImage.builder()
                    .product(product)
                    .url(photoPool.get(i % photoPool.size()))
                    .isPrimary(true)
                    .build();
            productImageRepository.save(image);
        }

        for (int i = 0; i < assets.size(); i++) {
            Asset asset = assets.get(i);
            AssetImage image = AssetImage.builder()
                    .asset(asset)
                    .url(photoPool.get((i + 3) % photoPool.size()))
                    .isPrimary(true)
                    .build();
            assetImageRepository.save(image);
        }
    }

    private Product createProduct(User user, Category category, String name, String brand, String desc, Long price, Integer stock) {
        return Product.builder()
                .user(user)
                .category(category)
                .productName(name)
                .brand(brand)
                .description(desc)
                .price(price)
                .stockQuantity(stock)
                .build();
    }

    private Asset createAsset(User user, Category category, String modelName, String brand, Long dailyRate, AssetStatus status, String serial) {
        return Asset.builder()
                .user(user)
                .category(category)
                .modelName(modelName)
                .brand(brand)
                .dailyRate(dailyRate)
                .status(status)
                .serialNumber(serial)
                .build();
    }
}
