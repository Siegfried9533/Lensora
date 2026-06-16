package com.camerashop.config;

import com.camerashop.entity.Asset;
import com.camerashop.entity.Asset.AssetStatus;
import com.camerashop.entity.AssetImage;
import com.camerashop.entity.Category;
import com.camerashop.entity.Category.EntityType;
import com.camerashop.entity.Product;
import com.camerashop.entity.ProductImage;
import com.camerashop.entity.User;
import com.camerashop.entity.User.Role;
import com.camerashop.repository.AssetImageRepository;
import com.camerashop.repository.AssetRepository;
import com.camerashop.repository.CategoryRepository;
import com.camerashop.repository.ProductImageRepository;
import com.camerashop.repository.ProductRepository;
import com.camerashop.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final String TEST_USER_ID = "u-00000000-0000-4000-a000-000000000001";
    private static final String ADMIN_USER_ID = "u-00000000-0000-4000-a000-000000000002";
    private static final String TEST_PASSWORD = "123456";

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
            return;
        }

        User testUser = User.builder()
                .userId(TEST_USER_ID)
                .userName("testuser")
                .email("testuser@lensora.com")
                .password(passwordEncoder.encode(TEST_PASSWORD))
                .role(Role.USER)
                .trustScore(85)
                .emailVerified(true)
                .build();

        User adminUser = User.builder()
                .userId(ADMIN_USER_ID)
                .userName("johndoe")
                .email("admin@lensora.com")
                .password(passwordEncoder.encode(TEST_PASSWORD))
                .role(Role.ADMIN)
                .trustScore(100)
                .emailVerified(true)
                .build();

        testUser = userRepository.save(testUser);
        adminUser = userRepository.save(adminUser);

        Map<String, Category> categories = seedCategories();
        Map<String, Product> products = seedProducts(categories, testUser, adminUser);
        Map<String, Asset> assets = seedAssets(categories, testUser, adminUser);

        seedProductImages(products);
        seedAssetImages(assets);
    }

    private Map<String, Category> seedCategories() {
        List<Category> categories = List.of(
                category("c1", "High-End Cameras", EntityType.PRODUCT),
                category("c2", "Medium Format", EntityType.PRODUCT),
                category("c3", "Mirrorless Cameras", EntityType.PRODUCT),
                category("c4", "Compact Cameras", EntityType.PRODUCT),
                category("c5", "Lenses", EntityType.PRODUCT),
                category("c6", "Accessories", EntityType.PRODUCT),
                category("a1", "Drones", EntityType.ASSET),
                category("a2", "Gimbals / Stabilizers", EntityType.ASSET),
                category("a3", "Audio Equipment", EntityType.ASSET),
                category("a4", "Photography Lighting", EntityType.ASSET),
                category("a5", "Rental Lenses", EntityType.ASSET),
                category("a6", "Tripods", EntityType.ASSET));

        return categoryRepository.saveAll(categories).stream()
                .collect(Collectors.toMap(
                        Category::getCategoryId,
                        category -> category,
                        (left, right) -> left,
                        LinkedHashMap::new));
    }

    private Map<String, Product> seedProducts(Map<String, Category> categories, User testUser, User adminUser) {
        List<Product> products = List.of(
                product("p-00000001-0000-4000-8000-000000000001", categories.get("c1"), testUser, "Leica M11",
                        "Leica", "A flagship Leica rangefinder camera with a 60MP full-frame sensor.", 10000L, 5),
                product("p-00000002-0000-4000-8000-000000000002", categories.get("c1"), adminUser, "Leica Q3",
                        "Leica", "A compact full-frame 60MP camera with a Summilux 28mm f/1.7 lens.", 10000L, 3),
                product("p-00000003-0000-4000-8000-000000000003", categories.get("c2"), testUser,
                        "Hasselblad X2D 100C", "Hasselblad",
                        "A 100MP medium format camera with a large-format sensor.", 10000L, 2),
                product("p-00000004-0000-4000-8000-000000000004", categories.get("c2"), adminUser,
                        "Fujifilm GFX 100S", "Fujifilm",
                        "A lightweight 102MP medium format camera for professional photography.", 10000L, 4),
                product("p-00000005-0000-4000-8000-000000000005", categories.get("c3"), testUser,
                        "Sony Alpha 1", "Sony",
                        "Flagship full-frame mirrorless camera with 50.1MP resolution and 30fps continuous shooting.",
                        10000L, 10),
                product("p-00000006-0000-4000-8000-000000000006", categories.get("c3"), testUser,
                        "Canon EOS R3", "Canon",
                        "A full-frame mirrorless camera built for sports and wildlife photography.",
                        10000L, 4),
                product("p-00000007-0000-4000-8000-000000000007", categories.get("c3"), adminUser, "Nikon Z9",
                        "Nikon", "A professional full-frame mirrorless camera.", 10000L, 7),
                product("p-00000008-0000-4000-8000-000000000008", categories.get("c3"), testUser,
                        "Fujifilm X-H2S", "Fujifilm",
                        "An APS-C stacked-sensor mirrorless camera optimized for video.",
                        10000L, 15),
                product("p-00000009-0000-4000-8000-000000000009", categories.get("c4"), testUser,
                        "Sony RX100 VII", "Sony", "A compact pocket camera with a 20.1MP 1-inch sensor.", 10000L,
                        20),
                product("p-00000010-0000-4000-8000-000000000010", categories.get("c5"), testUser,
                        "Sony FE 24-70mm f/2.8 GM II", "Sony",
                        "Sony's flagship standard professional zoom lens.",
                        10000L, 12),
                product("p-00000011-0000-4000-8000-000000000011", categories.get("c5"), adminUser,
                        "Canon RF 70-200mm f/2.8L IS USM", "Canon",
                        "A professional telephoto zoom lens for the Canon EOS R system.", 10000L, 8),
                product("p-00000012-0000-4000-8000-000000000012", categories.get("c6"), testUser,
                        "Peak Design Everyday Backpack V2", "Peak Design",
                        "An award-winning camera backpack with customizable dividers.", 10000L, 30),
                product("p-00000013-0000-4000-8000-000000000013", categories.get("c6"), testUser,
                        "ProGrade Digital CFexpress Type B 512GB", "ProGrade Digital",
                        "A high-speed memory card for 8K video recording.", 10000L, 25));

        return productRepository.saveAll(products).stream()
                .collect(Collectors.toMap(
                        Product::getProductId,
                        product -> product,
                        (left, right) -> left,
                        LinkedHashMap::new));
    }

    private Map<String, Asset> seedAssets(Map<String, Category> categories, User testUser, User adminUser) {
        List<Asset> assets = List.of(
                asset("a-00000001-0000-4000-8000-000000000001", categories.get("a1"), testUser,
                        "DJI Mavic 3 Pro", "DJI", 1500000L, AssetStatus.AVAILABLE, "M3P-00123"),
                asset("a-00000002-0000-4000-8000-000000000002", categories.get("a1"), adminUser,
                        "DJI Inspire 3", "DJI", 5000000L, AssetStatus.AVAILABLE, "INS3-00456"),
                asset("a-00000003-0000-4000-8000-000000000003", categories.get("a2"), testUser,
                        "DJI RS 3 Pro", "DJI", 450000L, AssetStatus.AVAILABLE, "RS3P-001"),
                asset("a-00000004-0000-4000-8000-000000000004", categories.get("a2"), testUser,
                        "Zhiyun Crane 3S", "Zhiyun", 350000L, AssetStatus.AVAILABLE, "ZY-C3S-88"),
                asset("a-00000005-0000-4000-8000-000000000005", categories.get("a3"), testUser,
                        "Sennheiser MKH 416", "Sennheiser", 300000L, AssetStatus.RENTED, "SN-416-09"),
                asset("a-00000006-0000-4000-8000-000000000006", categories.get("a3"), adminUser,
                        "Rode Wireless GO II", "Rode", 150000L, AssetStatus.AVAILABLE, "RD-WG2-11"),
                asset("a-00000007-0000-4000-8000-000000000007", categories.get("a4"), testUser,
                        "Aputure LS 600d Pro", "Aputure", 700000L, AssetStatus.AVAILABLE, "AP-600D-PRO"),
                asset("a-00000008-0000-4000-8000-000000000008", categories.get("a4"), adminUser,
                        "Profoto B10X Plus", "Profoto", 600000L, AssetStatus.RENTED, "PR-B10XP"),
                asset("a-00000009-0000-4000-8000-000000000009", categories.get("a5"), testUser,
                        "Canon EOS R5", "Canon", 800000L, AssetStatus.AVAILABLE, "R5-001239"),
                asset("a-00000010-0000-4000-8000-000000000010", categories.get("a5"), testUser,
                        "Sony FE 24-70mm f/2.8 GM II", "Sony", 400000L, AssetStatus.AVAILABLE, "GM2-45211"),
                asset("a-00000011-0000-4000-8000-000000000011", categories.get("a5"), adminUser,
                        "Canon RF 70-200mm f/2.8L IS USM", "Canon", 500000L, AssetStatus.AVAILABLE,
                        "RF72-1200"),
                asset("a-00000012-0000-4000-8000-000000000012", categories.get("a6"), testUser,
                        "Manfrotto 055 Carbon Fiber", "Manfrotto", 250000L, AssetStatus.AVAILABLE,
                        "MF-055CF-01"),
                asset("a-00000013-0000-4000-8000-000000000013", categories.get("a6"), adminUser,
                        "Peak Design Travel Tripod", "Peak Design", 200000L, AssetStatus.AVAILABLE,
                        "PD-TT-007"));

        return assetRepository.saveAll(assets).stream()
                .collect(Collectors.toMap(
                        Asset::getAssetId,
                        asset -> asset,
                        (left, right) -> left,
                        LinkedHashMap::new));
    }

    private void seedProductImages(Map<String, Product> products) {
        productImageRepository.saveAll(List.of(
                productImage("pi-0001-0000-4000-8000-000000000001", products.get("p-00000001-0000-4000-8000-000000000001"), "https://cdn.vjshop.vn/may-anh/mirrorless/leica/leica-m11-p/leica-m11-p-6-1500x1500.jpg", true),
                productImage("pi-0002-0000-4000-8000-000000000002", products.get("p-00000001-0000-4000-8000-000000000001"), "https://img.vistek.net/prodimgalt/large/462808_1.jpg", false),
                productImage("pi-0003-0000-4000-8000-000000000003", products.get("p-00000002-0000-4000-8000-000000000002"), "https://camerawest.com/cdn/shop/files/1138710-02.jpg?v=1684775996", true),
                productImage("pi-0004-0000-4000-8000-000000000004", products.get("p-00000002-0000-4000-8000-000000000002"), "https://techland.com.vn/public_folder/folder_image/uploads/2023/05/may-anh-Leica-Q3-10.jpg", false),
                productImage("pi-0005-0000-4000-8000-000000000005", products.get("p-00000003-0000-4000-8000-000000000003"), "https://store-na.hasselblad.com/cdn/shop/files/X2D_692x692_4x_cecaf8af-26d4-461a-9eaa-7f90ea4647ab_1108x1108@2x.png?v=1753844475", true),
                productImage("pi-0006-0000-4000-8000-000000000006", products.get("p-00000003-0000-4000-8000-000000000003"), "https://cdn.vjshop.vn/tin-tuc/ra-mat-hasselblad-x2d-ii-100c/ra-mat-hasselblad-x2d-ii-100c-5.jpg", false),
                productImage("pi-0007-0000-4000-8000-000000000007", products.get("p-00000004-0000-4000-8000-000000000004"), "https://tokyocamera.vn/wp-content/uploads/2024/05/Ra-Mat-Fujifilm-GFX-100S-II-1.jpg", true),
                productImage("pi-0008-0000-4000-8000-000000000008", products.get("p-00000004-0000-4000-8000-000000000004"), "https://www.cined.com/content/uploads/2024/05/FUJIFILM-GFX100S-II-3-1300x750.jpg", false),
                productImage("pi-0009-0000-4000-8000-000000000009", products.get("p-00000005-0000-4000-8000-000000000005"), "https://pos.nvncdn.com/9af890-179840/ps/May-anh-Sony-A1-Chinh-hang.jpg?v=1775724585", true),
                productImage("pi-0010-0000-4000-8000-000000000010", products.get("p-00000005-0000-4000-8000-000000000005"), "https://bizweb.dktcdn.net/100/369/815/products/maxresdefault-d91fc2e7-be94-4ca7-bc6b-d7fea58cea12-fb09edf7-a9a0-43ec-bd80-d31517e270eb-95a39d54-ab0b-478d-9853-63b605ad8566.jpg?v=1653042104137", false),
                productImage("pi-0011-0000-4000-8000-000000000011", products.get("p-00000006-0000-4000-8000-000000000006"), "https://product.hstatic.net/200000409445/product/1_8a80fdd4576b4003a13a3e193eecaa10.jpg", true),
                productImage("pi-0012-0000-4000-8000-000000000012", products.get("p-00000006-0000-4000-8000-000000000006"), "https://product.hstatic.net/200000409445/product/2_c987bb6669c1427782a31de257722c01_master.jpg", false),
                productImage("pi-0013-0000-4000-8000-000000000013", products.get("p-00000007-0000-4000-8000-000000000007"), "https://zshop.vn/images/thumbnails/1357/1000/detailed/91/1635510652_IMG_1633119.jpg", true),
                productImage("pi-0014-0000-4000-8000-000000000014", products.get("p-00000007-0000-4000-8000-000000000007"), "https://imaging.nikon.com/imaging/lineup/mirrorless/z_9/img/product_01_02.jpg", false),
                productImage("pi-0015-0000-4000-8000-000000000015", products.get("p-00000008-0000-4000-8000-000000000008"), "https://cdn.vjshop.vn/may-anh/mirrorless/fujifilm/fujifilm-x-h2s/fujifilm-xh2s.jpg", true),
                productImage("pi-0016-0000-4000-8000-000000000016", products.get("p-00000008-0000-4000-8000-000000000008"), "https://images.squarespace-cdn.com/content/v1/545012a9e4b0988576f6b699/3fb5c1af-5df0-4835-83f2-ed3e915403be/fujifilm-xh2s-xf-33mm.jpg", false),
                productImage("pi-0017-0000-4000-8000-000000000017", products.get("p-00000009-0000-4000-8000-000000000009"), "https://static.insales-cdn.com/images/products/1/3069/875113469/Sony_Cyber-shot_DSC-RX100_VII__dscrx100M7_.jpg", true),
                productImage("pi-0018-0000-4000-8000-000000000018", products.get("p-00000009-0000-4000-8000-000000000009"), "https://amateurphotographer.com/wp-content/uploads/sites/7/2019/07/DSC01677-scaled.jpg", false),
                productImage("pi-0019-0000-4000-8000-000000000019", products.get("p-00000010-0000-4000-8000-000000000010"), "https://cdn.vjshop.vn/ong-kinh/mirrorless/sony/ong-kinh-sony-fe-24-70mm-f28-gm-ii/fe-24-70-mm-f28-gm-ii-00.jpg", true),
                productImage("pi-0020-0000-4000-8000-000000000020", products.get("p-00000010-0000-4000-8000-000000000010"), "https://zshop.vn/images/thumbnails/1357/1000/detailed/98/1651055463_IMG_1739520.jpg", false),
                productImage("pi-0021-0000-4000-8000-000000000021", products.get("p-00000011-0000-4000-8000-000000000011"), "https://cdn.vjshop.vn/ong-kinh/mirrorless/canon/canon-rf-70-200mm-f2-8l-is-usm/anh-mo-ta/ong-kinh-canon-rf-70-200mm-f28l-is-usm-0.jpg", true),
                productImage("pi-0022-0000-4000-8000-000000000022", products.get("p-00000011-0000-4000-8000-000000000011"), "https://cdn.vjshop.vn/ong-kinh/mirrorless/canon/canon-rf-70-200mm-f2-8l-is-usm/canon-rf-70-200mm-f28l-is-usm-01.jpg", false),
                productImage("pi-0023-0000-4000-8000-000000000023", products.get("p-00000012-0000-4000-8000-000000000012"), "https://product.hstatic.net/200000863343/product/balo_peak_design_everyday_den__20l__ver_1-basic_bcababc5ba554aa6971b9003a18daf31.jpg", true),
                productImage("pi-0024-0000-4000-8000-000000000024", products.get("p-00000012-0000-4000-8000-000000000012"), "https://pos.nvncdn.com/68fa8b-42431/pc/20190508_iCrCOHZhtn39uFMDRQakbfdJ.jpg?v=1673290662", false),
                productImage("pi-0025-0000-4000-8000-000000000025", products.get("p-00000013-0000-4000-8000-000000000013"), "https://progradedigital.com/wp-content/uploads/2022/06/CFexpressB_Card_165GB_1700_1500_2pack.jpg", true)));
    }

    private void seedAssetImages(Map<String, Asset> assets) {
        assetImageRepository.saveAll(List.of(
                assetImage("ai-0001-0000-4000-8000-000000000001", assets.get("a-00000001-0000-4000-8000-000000000001"), "https://image.anhducdigital.vn/nhiep-anh/drone/flycam/dji-mavic-3-pro/dji-mavic-3-pro-with-dji-rc-remote-chinh-hang-01.jpg", true),
                assetImage("ai-0002-0000-4000-8000-000000000002", assets.get("a-00000001-0000-4000-8000-000000000001"), "https://tokyocamera.vn/wp-content/uploads/2023/04/DJI-Mavic-3-Pro-Fly-More-Combo-DJI-RC.jpg", false),
                assetImage("ai-0003-0000-4000-8000-000000000003", assets.get("a-00000002-0000-4000-8000-000000000002"), "https://cdn.vjshop.vn/flycam/dji/dji-inspire-3/anh-sp/dji-inspire-3-7-2500x2500.jpg", true),
                assetImage("ai-0004-0000-4000-8000-000000000004", assets.get("a-00000002-0000-4000-8000-000000000002"), "https://www.dronivo.de/media/image/product/11815/lg/dji-inspire-3.jpg", false),
                assetImage("ai-0005-0000-4000-8000-000000000005", assets.get("a-00000003-0000-4000-8000-000000000003"), "https://tokyocamera.vn/wp-content/uploads/2023/01/DJI-RS-3-Mini-6-Tokyo-Camera.jpg", true),
                assetImage("ai-0006-0000-4000-8000-000000000006", assets.get("a-00000003-0000-4000-8000-000000000003"), "https://cdn.vjshop.vn/thiet-bi-lam-video/gimbal/dji/dji-rs-3/dji-rs-3-pro/dji-rs-3-pro-comnbo-2000x2000.jpg", false),
                assetImage("ai-0007-0000-4000-8000-000000000007", assets.get("a-00000004-0000-4000-8000-000000000004"), "https://product.hstatic.net/200000409445/product/6_ee56890ed2c643d080165ab26fe43a84.jpg", true),
                assetImage("ai-0008-0000-4000-8000-000000000008", assets.get("a-00000004-0000-4000-8000-000000000004"), "https://store.zhiyun-tech.com/cdn/shop/files/3S-aa5c3d75-84f9-4515-8d03-2878f6a7d85b-_6.jpg?v=1708584094&width=1780", false),
                assetImage("ai-0009-0000-4000-8000-000000000009", assets.get("a-00000005-0000-4000-8000-000000000005"), "https://www.adorama.com/images/Large/SEMKH416.JPG", true),
                assetImage("ai-0010-0000-4000-8000-000000000010", assets.get("a-00000005-0000-4000-8000-000000000005"), "https://cdn.uc.assets.prezly.com/b76cfabc-458a-4c75-bf6b-402f19d51530/-/crop/3287x1733/0,516/-/preview/-/resize/992/-/format/png/-/progressive/yes/-/quality/smart/", false),
                assetImage("ai-0011-0000-4000-8000-000000000011", assets.get("a-00000006-0000-4000-8000-000000000006"), "https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQsYDPalJ4N4EtCLpEjUI-PxiyVraNlVmzFTg&s", true),
                assetImage("ai-0012-0000-4000-8000-000000000012", assets.get("a-00000006-0000-4000-8000-000000000006"), "https://pos.nvncdn.com/3d97f1-13296/ps/20231025_zWEfTKX73m.webp?v=1698232858", false),
                assetImage("ai-0013-0000-4000-8000-000000000013", assets.get("a-00000007-0000-4000-8000-000000000007"), "https://tokyocamera.vn/wp-content/uploads/2022/05/Aputure-LS-600d-Pro-Daylight-LED-Light-V-mount-1.jpg", true),
                assetImage("ai-0014-0000-4000-8000-000000000014", assets.get("a-00000007-0000-4000-8000-000000000007"), "https://m.media-amazon.com/images/I/714nDzpHSrL._AC_UF894,1000_QL80_.jpg", false),
                assetImage("ai-0015-0000-4000-8000-000000000015", assets.get("a-00000008-0000-4000-8000-000000000008"), "https://711srg4.711int.com/read/62068c61b2815?sig=t4xJxa0UejYfLiv3Rj0LxfHODIA%3D&pubKey=EsdjgASDerwdsgFGcesdaSDZQWER34SFPxE%25SFDGjhgsesdfg", true),
                assetImage("ai-0016-0000-4000-8000-000000000016", assets.get("a-00000008-0000-4000-8000-000000000008"), "https://www.bhphotovideo.com/images/fb/profoto_b10x_ocf_flash_head_1769136.jpg", false),
                assetImage("ai-0017-0000-4000-8000-000000000017", assets.get("a-00000009-0000-4000-8000-000000000009"), "https://cdn.vjshop.vn/may-anh/mirrorless/canon/canon-eos-r5-mark-ii/canon-eos-r5-mark-ii.jpg", true),
                assetImage("ai-0018-0000-4000-8000-000000000018", assets.get("a-00000009-0000-4000-8000-000000000009"), "https://www.cinemachine.com.au/cdn/shop/files/Untitleddesign_22_600x600_crop_center.png?v=1724826394", false),
                assetImage("ai-0019-0000-4000-8000-000000000019", assets.get("a-00000010-0000-4000-8000-000000000010"), "https://cdn.vjshop.vn/ong-kinh/mirrorless/sony/ong-kinh-sony-fe-24-70mm-f28-gm-ii/fe-24-70-mm-f28-gm-ii-00.jpg", true),
                assetImage("ai-0020-0000-4000-8000-000000000020", assets.get("a-00000010-0000-4000-8000-000000000010"), "https://zshop.vn/images/thumbnails/1357/1000/detailed/98/1651055463_IMG_1739520.jpg", false),
                assetImage("ai-0021-0000-4000-8000-000000000021", assets.get("a-00000011-0000-4000-8000-000000000011"), "https://cdn.vjshop.vn/ong-kinh/mirrorless/canon/canon-rf-70-200mm-f2-8l-is-usm/anh-mo-ta/ong-kinh-canon-rf-70-200mm-f28l-is-usm-0.jpg", true),
                assetImage("ai-0022-0000-4000-8000-000000000022", assets.get("a-00000011-0000-4000-8000-000000000011"), "https://cdn.vjshop.vn/ong-kinh/mirrorless/canon/canon-rf-70-200mm-f2-8l-is-usm/canon-rf-70-200mm-f28l-is-usm-01.jpg", false),
                assetImage("ai-0023-0000-4000-8000-000000000023", assets.get("a-00000012-0000-4000-8000-000000000012"), "https://pos.nvncdn.com/91002e-15402/ps/20241122_ErehNyCk4C.webp?v=1732272755", true),
                assetImage("ai-0024-0000-4000-8000-000000000024", assets.get("a-00000012-0000-4000-8000-000000000012"), "https://http2.mlstatic.com/D_Q_NP_2X_936622-MLA99455845460_112025-P.webp", false),
                assetImage("ai-0025-0000-4000-8000-000000000025", assets.get("a-00000013-0000-4000-8000-000000000013"), "https://zshop.vn/images/thumbnails/1357/1000/detailed/55/1563493561_1495136.jpg", true),
                assetImage("ai-0026-0000-4000-8000-000000000026", assets.get("a-00000013-0000-4000-8000-000000000013"), "https://giangduydat.vn/product/chan-peakdesign-travel-tripod.jpg", false)));
    }

    private Category category(String id, String name, EntityType type) {
        return Category.builder()
                .categoryId(id)
                .categoryName(name)
                .type(type)
                .build();
    }

    private Product product(String id, Category category, User user, String name, String brand, String description,
            Long price, Integer stock) {
        return Product.builder()
                .productId(id)
                .category(category)
                .user(user)
                .productName(name)
                .brand(brand)
                .description(description)
                .price(price)
                .stockQuantity(stock)
                .build();
    }

    private Asset asset(String id, Category category, User user, String modelName, String brand, Long dailyRate,
            AssetStatus status, String serialNumber) {
        return Asset.builder()
                .assetId(id)
                .category(category)
                .user(user)
                .modelName(modelName)
                .brand(brand)
                .dailyRate(dailyRate)
                .status(status)
                .serialNumber(serialNumber)
                .build();
    }

    private ProductImage productImage(String id, Product product, String url, boolean primary) {
        return ProductImage.builder()
                .imageId(id)
                .product(product)
                .url(url)
                .isPrimary(primary)
                .build();
    }

    private AssetImage assetImage(String id, Asset asset, String url, boolean primary) {
        return AssetImage.builder()
                .imageId(id)
                .asset(asset)
                .url(url)
                .isPrimary(primary)
                .build();
    }
}
