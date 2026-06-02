package com.camerashop.config;

import com.camerashop.entity.*;
import com.camerashop.entity.User.Role;
import com.camerashop.entity.Category.EntityType;
import com.camerashop.entity.Asset.AssetStatus;
import com.camerashop.entity.Notification.NotificationType;
import com.camerashop.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

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

    @Autowired
    private NotificationRepository notificationRepository;

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) {
            seedAdditionalMockProducts();
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
                Category.builder().categoryId("c9").categoryName("Accessories").type(EntityType.PRODUCT).build());
        categoryRepository.saveAll(productCategories);
        categoryRepository.flush();

        List<Category> assetCategories = Arrays.asList(
                Category.builder().categoryId("c4").categoryName("Camera Body").type(EntityType.ASSET).build(),
                Category.builder().categoryId("c5").categoryName("Lens").type(EntityType.ASSET).build(),
                Category.builder().categoryId("c6").categoryName("Lighting").type(EntityType.ASSET).build(),
                Category.builder().categoryId("c10").categoryName("Stabilizer").type(EntityType.ASSET).build(),
                Category.builder().categoryId("c11").categoryName("Audio").type(EntityType.ASSET).build());
        categoryRepository.saveAll(assetCategories);
        categoryRepository.flush();

        // Create Products
        List<Product> products = Arrays.asList(
                createProduct(testUser, productCategories.get(0), "LEICA M11", "Leica",
                        "The newest digital rangefinder from Leica combining classic design with contemporary technology.",
                        10000L, 5),
                createProduct(testUser, productCategories.get(1), "HASSELBLAD X2D", "Hasselblad",
                        "100-megapixel medium format mirrorless camera for ultimate image quality.", 10000L, 2),
                createProduct(testUser, productCategories.get(2), "SONY α1", "Sony",
                        "The one. Flagship full-frame mirrorless camera with 50.1MP and 30fps shooting.", 10000L,
                        10),
                createProduct(testUser, productCategories.get(2), "CANON EOS R3", "Canon",
                        "High performance sports and wildlife mirrorless camera.", 10000L, 4),
                createProduct(testUser, productCategories.get(2), "NIKON Z9", "Nikon",
                        "Professional full-frame mirrorless without mechanical shutter.", 10000L, 7),
                createProduct(testUser, productCategories.get(2), "FUJIFILM X-H2S", "Fujifilm",
                        "High speed APS-C camera with stacked sensor technology.", 10000L, 15),
                createProduct(testUser, productCategories.get(3), "GoPro HERO 12 Black", "GoPro",
                        "The ultimate action camera for extreme sports.", 10000L, 50),
                createProduct(testUser, productCategories.get(3), "DJI Osmo Action 4", "DJI",
                        "Excellent low light action cam.", 10000L, 45),
                createProduct(testUser, productCategories.get(4), "DJI Mavic 3 Pro", "DJI",
                        "Triple camera drone for professional cinematic shots.", 10000L, 8),
                createProduct(testUser, productCategories.get(4), "DJI Mini 4 Pro", "DJI",
                        "Mini camera drone under 249g with omnidirectional obstacle sensing.", 10000L, 20),
                createProduct(testUser, productCategories.get(5), "Peak Design Everyday Backpack", "Peak Design",
                        "Award winning camera bags for everyday carry.", 10000L, 30),
                createProduct(testUser, productCategories.get(5), "ProGrade CFexpress Type B 512GB", "ProGrade",
                        "Ultra fast memory card for 8K video.", 10000L, 12));
        productRepository.saveAll(products);

        // Create Assets
        List<Asset> assets = Arrays.asList(
                createAsset(testUser, assetCategories.get(0), "Canon EOS R5", "Canon", 800000L, AssetStatus.AVAILABLE,
                        "R5-001239"),
                createAsset(testUser, assetCategories.get(1), "Sony FE 24-70mm f/2.8 GM II", "Sony", 400000L,
                        AssetStatus.AVAILABLE, "GM2-45211"),
                createAsset(testUser, assetCategories.get(0), "RED Komodo 6K", "RED", 2500000L, AssetStatus.RENTED,
                        "KMD-99120"),
                createAsset(testUser, assetCategories.get(0), "ARRI Alexa Mini LF", "ARRI", 8000000L,
                        AssetStatus.AVAILABLE, "ALX-10332"),
                createAsset(testUser, assetCategories.get(1), "Canon RF 70-200mm f/2.8", "Canon", 500000L,
                        AssetStatus.AVAILABLE, "RF72-1200"),
                createAsset(testUser, assetCategories.get(1), "Sigma Art 35mm f/1.4", "Sigma", 200000L,
                        AssetStatus.AVAILABLE, "SG-ART35-1"),
                createAsset(testUser, assetCategories.get(2), "Aputure LS 600d Pro", "Aputure", 700000L,
                        AssetStatus.AVAILABLE, "AP-600D-PRO"),
                createAsset(testUser, assetCategories.get(2), "Profoto B10X Plus", "Profoto", 600000L,
                        AssetStatus.RENTED, "PR-B10XP"),
                createAsset(testUser, assetCategories.get(3), "DJI RS 3 Pro Gimbal", "DJI", 450000L,
                        AssetStatus.AVAILABLE, "RS3P-001"),
                createAsset(testUser, assetCategories.get(3), "Zhiyun Crane 3S", "Zhiyun", 350000L,
                        AssetStatus.AVAILABLE, "ZY-C3S-88"),
                createAsset(testUser, assetCategories.get(4), "Sennheiser MKH 416", "Sennheiser", 300000L,
                        AssetStatus.RENTED, "SN-416-09"),
                createAsset(testUser, assetCategories.get(4), "Rode Wireless GO II", "Rode", 150000L,
                        AssetStatus.AVAILABLE, "RD-WG2-11"));
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
                "https://images.unsplash.com/photo-1520390138845-fd2d229dd553?w=800");

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

        // Create seed notifications for test users
        List<Notification> seedNotifications = Arrays.asList(
                Notification.builder()
                        .notificationId(java.util.UUID.randomUUID().toString())
                        .user(testUser)
                        .title("Welcome to Lensora!")
                        .message(
                                "Thank you for joining Lensora! Browse our collection of premium cameras, lenses, and equipment. Feel free to explore and find the perfect gear for your needs.")
                        .type(NotificationType.SYSTEM)
                        .isRead(false)
                        .isActionRequired(false)
                        .build(),
                Notification.builder()
                        .notificationId(java.util.UUID.randomUUID().toString())
                        .user(testUser)
                        .title("New Arrivals: Leica M11")
                        .message(
                                "Check out the latest Leica M11 digital rangefinder — now available in our shop! Classic design meets cutting-edge technology.")
                        .type(NotificationType.PROMOTION)
                        .isRead(false)
                        .isActionRequired(false)
                        .build(),
                Notification.builder()
                        .notificationId(java.util.UUID.randomUUID().toString())
                        .user(adminUser)
                        .title("Welcome to Lensora!")
                        .message(
                                "Thank you for joining Lensora! As an admin, you can manage products, orders, and users from your dashboard.")
                        .type(NotificationType.SYSTEM)
                        .isRead(false)
                        .isActionRequired(false)
                        .build());
        notificationRepository.saveAll(seedNotifications);
        seedAdditionalMockProducts();
    }

    private void seedAdditionalMockProducts() {
        User seller = userRepository.findByUserName("testuser")
                .orElseGet(() -> userRepository.findAll().stream().findFirst().orElse(null));
        if (seller == null) {
            return;
        }

        Map<String, Category> productCategories = ensureProductCategories();
        Set<String> existingProductNames = new HashSet<>();
        for (Product product : productRepository.findAll()) {
            if (product.getProductName() != null) {
                existingProductNames.add(product.getProductName().toLowerCase(Locale.ROOT));
            }
        }

        List<MockProductSpec> mockProducts = Arrays.asList(
                new MockProductSpec("c1", "Leica SL3", "Leica",
                        "Full-frame hybrid camera for premium stills and cinematic 8K production.", 10000L, 6),
                new MockProductSpec("c1", "Leica Q3 43", "Leica",
                        "Fixed-lens full-frame camera with a natural 43mm field of view and refined rangefinder feel.",
                        10000L, 4),
                new MockProductSpec("c1", "Sony RX1R III", "Sony",
                        "Pocketable premium full-frame camera for street, travel, and documentary photography.", 10000L,
                        8),
                new MockProductSpec("c1", "Canon EOS-1D X Mark III", "Canon",
                        "Rugged flagship DSLR body built for sports, wildlife, and newsroom assignments.", 10000L, 5),
                new MockProductSpec("c1", "Nikon D850", "Nikon",
                        "High-resolution professional DSLR with excellent dynamic range and dependable battery life.",
                        10000L, 9),
                new MockProductSpec("c1", "Ricoh GR IIIx Urban Edition", "Ricoh",
                        "Compact APS-C street camera with fast snap focus and a sharp 40mm-equivalent lens.", 10000L,
                        15),
                new MockProductSpec("c1", "Panasonic Lumix LX100 II", "Panasonic",
                        "Premium compact camera with large sensor, tactile controls, and Leica DC zoom lens.", 10000L,
                        11),

                new MockProductSpec("c2", "Fujifilm GFX 100 II", "Fujifilm",
                        "Medium format mirrorless body with 102MP resolution and fast phase-detect autofocus.", 10000L,
                        3),
                new MockProductSpec("c2", "Fujifilm GFX 50S II", "Fujifilm",
                        "Portable medium format body with stabilized 51.4MP sensor for studio and landscape work.",
                        10000L, 5),
                new MockProductSpec("c2", "Hasselblad X1D II 50C", "Hasselblad",
                        "Minimal medium format camera with natural color science and lightweight travel design.",
                        10000L, 2),
                new MockProductSpec("c2", "Pentax 645Z", "Pentax",
                        "Weather-sealed medium format DSLR for commercial, product, and outdoor photography.",
                        10000L, 3),
                new MockProductSpec("c2", "Phase One XF IQ4 150MP", "Phase One",
                        "Ultra-high-resolution medium format system for demanding studio capture.", 10000L, 1),

                new MockProductSpec("c3", "Sony A7R V", "Sony",
                        "61MP full-frame mirrorless camera with advanced subject recognition and strong stabilization.",
                        10000L, 12),
                new MockProductSpec("c3", "Sony A7 IV", "Sony",
                        "Balanced full-frame hybrid camera for creators who shoot both photos and video.", 10000L, 18),
                new MockProductSpec("c3", "Sony A7S III", "Sony",
                        "Low-light video-focused full-frame camera with reliable 4K 120fps recording.", 10000L, 10),
                new MockProductSpec("c3", "Canon EOS R5 Mark II", "Canon",
                        "High-resolution EOS R body for commercial photography, events, and advanced video work.",
                        10000L, 8),
                new MockProductSpec("c3", "Canon EOS R6 Mark II", "Canon",
                        "Fast full-frame hybrid mirrorless camera for weddings, portraits, and sports.", 10000L, 14),
                new MockProductSpec("c3", "Canon EOS R8", "Canon",
                        "Lightweight full-frame camera with excellent autofocus for everyday creators.", 10000L, 16),
                new MockProductSpec("c3", "Nikon Z8", "Nikon",
                        "Compact professional mirrorless body with flagship Z9 performance in a smaller package.",
                        10000L, 9),
                new MockProductSpec("c3", "Nikon Zf", "Nikon",
                        "Retro-styled full-frame camera with modern autofocus, stabilization, and video tools.",
                        10000L, 13),
                new MockProductSpec("c3", "Fujifilm X-T5", "Fujifilm",
                        "Compact APS-C mirrorless camera with 40MP sensor and classic tactile controls.", 10000L, 20),
                new MockProductSpec("c3", "Fujifilm X-S20", "Fujifilm",
                        "Creator-friendly APS-C hybrid body with strong video features and long battery life.",
                        10000L, 22),
                new MockProductSpec("c3", "Panasonic Lumix S5IIX", "Panasonic",
                        "Full-frame mirrorless camera for filmmakers with phase-detect autofocus and open-gate video.",
                        10000L, 11),
                new MockProductSpec("c3", "OM System OM-1 Mark II", "OM System",
                        "Weather-sealed Micro Four Thirds flagship for wildlife, macro, and travel shooting.",
                        10000L, 10),
                new MockProductSpec("c3", "Sigma fp L", "Sigma",
                        "Ultra-compact full-frame camera with 61MP sensor and modular cinema workflow.", 10000L, 7),
                new MockProductSpec("c3", "Nikon Z6 III", "Nikon",
                        "Hybrid full-frame camera with fast readout, strong video tools, and reliable autofocus.",
                        10000L, 15),

                new MockProductSpec("c7", "GoPro HERO 13 Black", "GoPro",
                        "Rugged action camera for high-frame-rate sports, travel, and outdoor capture.", 10000L, 35),
                new MockProductSpec("c7", "DJI Osmo Action 5 Pro", "DJI",
                        "Action camera with long battery life, strong stabilization, and dual OLED screens.", 10000L,
                        30),
                new MockProductSpec("c7", "Insta360 Ace Pro 2", "Insta360",
                        "Wide-angle action camera tuned for low-light adventure footage and quick sharing.", 10000L,
                        28),
                new MockProductSpec("c7", "Insta360 X4", "Insta360",
                        "8K 360-degree action camera for reframing, invisible selfie-stick shots, and VR capture.",
                        10000L, 25),
                new MockProductSpec("c7", "DJI Osmo Pocket 3", "DJI",
                        "Pocket gimbal camera with 1-inch sensor for handheld vlogging and travel content.", 10000L,
                        24),
                new MockProductSpec("c7", "Insta360 GO 3S", "Insta360",
                        "Tiny wearable action camera for POV clips, behind-the-scenes footage, and travel videos.",
                        10000L, 32),
                new MockProductSpec("c7", "Akaso Brave 8", "Akaso",
                        "Affordable action camera for casual sports, water activities, and family trips.", 10000L, 40),
                new MockProductSpec("c7", "DJI Osmo Mobile SE", "DJI",
                        "Phone stabilizer for creators who want smooth mobile video with simple controls.", 10000L,
                        45),

                new MockProductSpec("c8", "DJI Air 3", "DJI",
                        "Dual-camera drone with long flight time and flexible wide-to-tele capture.", 10000L, 18),
                new MockProductSpec("c8", "DJI Avata 2", "DJI",
                        "Compact FPV drone for immersive flight footage and dynamic creator shots.", 10000L, 14),
                new MockProductSpec("c8", "DJI Neo", "DJI",
                        "Small personal drone for quick follow shots, travel clips, and social media video.", 10000L,
                        26),
                new MockProductSpec("c8", "Autel EVO Lite Plus", "Autel",
                        "Portable drone with large sensor and strong low-light aerial image quality.", 10000L, 8),
                new MockProductSpec("c8", "DJI Mavic 3 Classic", "DJI",
                        "Hasselblad-camera drone for creators who need cinematic aerial footage.", 10000L, 9),
                new MockProductSpec("c8", "DJI Mini 3 Pro", "DJI",
                        "Sub-249g drone with vertical shooting, obstacle sensing, and travel-friendly design.",
                        10000L, 21),
                new MockProductSpec("c8", "DJI Matrice 30T", "DJI",
                        "Enterprise drone platform with thermal camera, zoom camera, and weather resistance.",
                        10000L, 2),

                new MockProductSpec("c9", "Sony TOUGH SDXC 128GB", "Sony",
                        "Durable UHS-II memory card for fast bursts and reliable 4K recording.", 10000L, 60),
                new MockProductSpec("c9", "SanDisk Extreme PRO SDXC 256GB", "SanDisk",
                        "High-capacity SD card for photo sessions, weddings, events, and travel work.", 10000L, 70),
                new MockProductSpec("c9", "Angelbird AV PRO CFexpress 1TB", "Angelbird",
                        "Professional CFexpress card for 8K recording and sustained high-speed capture.", 10000L,
                        18),
                new MockProductSpec("c9", "Manfrotto Befree Advanced", "Manfrotto",
                        "Travel tripod with compact folding legs and stable ball head for mirrorless kits.",
                        10000L, 25),
                new MockProductSpec("c9", "Peak Design Travel Tripod Carbon", "Peak Design",
                        "Slim carbon-fiber tripod for photographers who need stability without bulk.", 10000L, 16),
                new MockProductSpec("c9", "Lowepro ProTactic BP 450 AW II", "Lowepro",
                        "Modular camera backpack with rugged access points and all-weather protection.", 10000L, 20),
                new MockProductSpec("c9", "Peak Design Slide Strap", "Peak Design",
                        "Adjustable camera strap for sling, neck, and shoulder carry styles.", 10000L, 45),
                new MockProductSpec("c9", "Rode VideoMic Pro Plus", "Rode",
                        "On-camera shotgun microphone for interviews, vlogs, and compact video rigs.", 10000L, 30),
                new MockProductSpec("c9", "Godox V1 Flash", "Godox",
                        "Round-head TTL flash with rechargeable battery and soft, even light quality.", 10000L, 28),
                new MockProductSpec("c9", "SmallRig Camera Cage Kit", "SmallRig",
                        "Modular cage kit for mounting monitors, handles, microphones, and accessories.",
                        10000L, 34),
                new MockProductSpec("c9", "Atomos Ninja V Plus", "Atomos",
                        "External monitor-recorder for pro video workflows and high-quality recording.", 10000L, 12),
                new MockProductSpec("c9", "Nanlite PavoTube II 30C", "Nanlite",
                        "RGB tube light for portraits, product photography, music videos, and creative sets.",
                        10000L, 18),
                new MockProductSpec("c9", "Aputure MC RGBWW Light", "Aputure",
                        "Pocket RGBWW LED panel for accent lighting, practical effects, and travel kits.", 10000L,
                        50),
                new MockProductSpec("c9", "Blackmagic Design Video Assist 12G", "Blackmagic Design",
                        "Monitor-recorder with scopes, SDI/HDMI inputs, and professional ProRes recording.",
                        10000L, 7));

        List<String> imagePool = Arrays.asList(
                "https://images.unsplash.com/photo-1516035069371-29a1b244cc32?w=800&q=80",
                "https://images.unsplash.com/photo-1516961642265-531546e84af2?w=800&q=80",
                "https://images.unsplash.com/photo-1502920917128-1aa500764cbd?w=800&q=80",
                "https://images.unsplash.com/photo-1519183071298-a29601bc7c68?w=800&q=80",
                "https://images.unsplash.com/photo-1520390138845-fd2d229dd553?w=800&q=80",
                "https://images.unsplash.com/photo-1526170375885-4d9baaa10b5f?w=800&q=80",
                "https://images.unsplash.com/photo-1542567455-cd733f23fbb1?w=800&q=80",
                "https://images.unsplash.com/photo-1500530855697-b586d89ba3ee?w=800&q=80",
                "https://images.unsplash.com/photo-1506947411487-a56738267384?w=800&q=80",
                "https://images.unsplash.com/photo-1527977966376-1c8408f9f108?w=800&q=80",
                "https://images.unsplash.com/photo-1484704849700-f032a568e944?w=800&q=80",
                "https://images.unsplash.com/photo-1495121553079-4c61bcce1894?w=800&q=80");

        List<Product> createdProducts = new ArrayList<>();
        int imageIndex = 0;
        for (MockProductSpec spec : mockProducts) {
            if (existingProductNames.contains(spec.name.toLowerCase(Locale.ROOT))) {
                imageIndex++;
                continue;
            }
            Category category = productCategories.get(spec.categoryId);
            if (category == null) {
                imageIndex++;
                continue;
            }

            Product product = productRepository.save(createProduct(seller, category, spec.name, spec.brand,
                    spec.description, spec.price, spec.stock));
            productImageRepository.save(ProductImage.builder()
                    .product(product)
                    .url(imagePool.get(imageIndex % imagePool.size()))
                    .isPrimary(true)
                    .build());
            createdProducts.add(product);
            existingProductNames.add(spec.name.toLowerCase(Locale.ROOT));
            imageIndex++;
        }
        if (!createdProducts.isEmpty()) {
            productRepository.flush();
        }
    }

    private Map<String, Category> ensureProductCategories() {
        Map<String, Category> categories = new HashMap<>();
        categories.put("c1", ensureProductCategory("c1", "Premium Camera"));
        categories.put("c2", ensureProductCategory("c2", "Medium Format"));
        categories.put("c3", ensureProductCategory("c3", "Mirrorless"));
        categories.put("c7", ensureProductCategory("c7", "Action Camera"));
        categories.put("c8", ensureProductCategory("c8", "Drone"));
        categories.put("c9", ensureProductCategory("c9", "Accessories"));
        return categories;
    }

    private Category ensureProductCategory(String categoryId, String categoryName) {
        return categoryRepository.findById(categoryId)
                .orElseGet(() -> categoryRepository.save(Category.builder()
                        .categoryId(categoryId)
                        .categoryName(categoryName)
                        .type(EntityType.PRODUCT)
                        .build()));
    }

    private static class MockProductSpec {
        final String categoryId;
        final String name;
        final String brand;
        final String description;
        final Long price;
        final Integer stock;

        MockProductSpec(String categoryId, String name, String brand, String description, Long price, Integer stock) {
            this.categoryId = categoryId;
            this.name = name;
            this.brand = brand;
            this.description = description;
            this.price = price;
            this.stock = stock;
        }
    }

    private Product createProduct(User user, Category category, String name, String brand, String desc, Long price,
            Integer stock) {
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

    private Asset createAsset(User user, Category category, String modelName, String brand, Long dailyRate,
            AssetStatus status, String serial) {
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
