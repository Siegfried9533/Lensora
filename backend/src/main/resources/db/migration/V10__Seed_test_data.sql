-- ============================================================================
-- V10__Seed_test_data.sql
-- Du lieu thu nghiem chuan hoa cho PostgreSQL (Du an Lensora)
-- ============================================================================

-- 1. Xoa du lieu cu theo thu tu de tranh xung dot khoa ngoai.
DELETE FROM payment_transactions;
DELETE FROM order_items;
DELETE FROM orders;
DELETE FROM rentals;
DELETE FROM cart_items;
DELETE FROM favorites;
DELETE FROM notifications;
DELETE FROM payment_methods;
DELETE FROM user_addresses;
DELETE FROM email_verification_tokens;
DELETE FROM password_reset_tokens;
DELETE FROM reviews;
DELETE FROM product_images;
DELETE FROM asset_images;
DELETE FROM products;
DELETE FROM assets;
DELETE FROM categories;
DELETE FROM users;

-- ============================================================================
-- 2. Chen nguoi dung mau (mat khau dang nhap: 123456)
-- ============================================================================
INSERT INTO users (user_id, user_name, email, password, role, trust_score, email_verified, created_at, updated_at) VALUES
    ('u-00000000-0000-4000-a000-000000000001', 'testuser', 'testuser@lensora.com', '$2a$10$abcdefghijklmnopqrstuuxD0vCwu/f2ahNVJLL35ZYA2xuiRyPH6', 'USER', 85, true, NOW(), NOW()),
    ('u-00000000-0000-4000-a000-000000000002', 'johndoe', 'admin@lensora.com', '$2a$10$abcdefghijklmnopqrstuuxD0vCwu/f2ahNVJLL35ZYA2xuiRyPH6', 'ADMIN', 100, true, NOW(), NOW());

-- ============================================================================
-- 3. Chen danh muc
-- ============================================================================
INSERT INTO categories (category_id, category_name, type) VALUES
    ('c1', 'May anh cao cap',       'PRODUCT'),
    ('c2', 'Medium Format',          'PRODUCT'),
    ('c3', 'May anh Mirrorless',    'PRODUCT'),
    ('c4', 'May anh Compact',       'PRODUCT'),
    ('c5', 'Ong kinh',              'PRODUCT'),
    ('c6', 'Phu kien',              'PRODUCT'),
    ('a1', 'Flycam',                'ASSET'),
    ('a2', 'Gimbal / On dinh',     'ASSET'),
    ('a3', 'Thiet bi am thanh',    'ASSET'),
    ('a4', 'Den chup anh',         'ASSET'),
    ('a5', 'Ong kinh cho thue',    'ASSET'),
    ('a6', 'Chan may anh',         'ASSET');

-- ============================================================================
-- 4. Chen san pham
-- ============================================================================
INSERT INTO products (product_id, category_id, user_id, product_name, brand, description, price, stock_quantity, created_at) VALUES
    ('p-00000001-0000-4000-8000-000000000001', 'c1', 'u-00000000-0000-4000-a000-000000000001', 'Leica M11', 'Leica', 'May anh rangefinder dinh cao cua Leica voi sensor 60MP full-frame.', 10000, 5, NOW()),
    ('p-00000002-0000-4000-8000-000000000002', 'c1', 'u-00000000-0000-4000-a000-000000000002', 'Leica Q3', 'Leica', 'May anh compact full-frame 60MP voi ong kinh Summilux 28mm f/1.7.', 10000, 3, NOW()),
    ('p-00000003-0000-4000-8000-000000000003', 'c2', 'u-00000000-0000-4000-a000-000000000001', 'Hasselblad X2D 100C', 'Hasselblad', 'May anh medium format 100 megapixel voi sensor trung format.', 10000, 2, NOW()),
    ('p-00000004-0000-4000-8000-000000000004', 'c2', 'u-00000000-0000-4000-a000-000000000002', 'Fujifilm GFX 100S', 'Fujifilm', 'May anh medium format 102MP trong lo the nhat thi truong.', 10000, 4, NOW()),
    ('p-00000005-0000-4000-8000-000000000005', 'c3', 'u-00000000-0000-4000-a000-000000000001', 'Sony Alpha 1', 'Sony', 'Flagship mirrorless full-frame 50.1MP, quay lien tuc 30fps.', 10000, 10, NOW()),
    ('p-00000006-0000-4000-8000-000000000006', 'c3', 'u-00000000-0000-4000-a000-000000000001', 'Canon EOS R3', 'Canon', 'May anh mirrorless full-frame cho the thao va hoang da.', 10000, 4, NOW()),
    ('p-00000007-0000-4000-8000-000000000007', 'c3', 'u-00000000-0000-4000-a000-000000000002', 'Nikon Z9', 'Nikon', 'May anh mirrorless full-frame chuyen nghiep.', 10000, 7, NOW()),
    ('p-00000008-0000-4000-8000-000000000008', 'c3', 'u-00000000-0000-4000-a000-000000000001', 'Fujifilm X-H2S', 'Fujifilm', 'May anh mirrorless APS-C sensor stacked toi uu cho video.', 10000, 15, NOW()),
    ('p-00000009-0000-4000-8000-000000000009', 'c4', 'u-00000000-0000-4000-a000-000000000001', 'Sony RX100 VII', 'Sony', 'May anh compact nho gon voi sensor 1-inch 20.1MP.', 10000, 20, NOW()),
    ('p-00000010-0000-4000-8000-000000000010', 'c5', 'u-00000000-0000-4000-a000-000000000001', 'Sony FE 24-70mm f/2.8 GM II', 'Sony', 'Ong kinh zoom tieu chuyen nhat cua Sony.', 10000, 12, NOW()),
    ('p-00000011-0000-4000-8000-000000000011', 'c5', 'u-00000000-0000-4000-a000-000000000002', 'Canon RF 70-200mm f/2.8L IS USM', 'Canon', 'Ong kinh tele zoom chuyen nghiep cho he thong Canon EOS R.', 10000, 8, NOW()),
    ('p-00000012-0000-4000-8000-000000000012', 'c6', 'u-00000000-0000-4000-a000-000000000001', 'Peak Design Everyday Backpack V2', 'Peak Design', 'Balo chua do anh giai thuong thiet ke, cho phep tu tuy chinh.', 10000, 30, NOW()),
    ('p-00000013-0000-4000-8000-000000000013', 'c6', 'u-00000000-0000-4000-a000-000000000001', 'ProGrade Digital CFexpress Type B 512GB', 'ProGrade Digital', 'The nho toc do cao cho quay video 8K.', 10000, 25, NOW());

-- ============================================================================
-- 5. Chen tai san
-- ============================================================================
INSERT INTO assets (asset_id, category_id, user_id, model_name, brand, daily_rate, status, serial_number, created_at) VALUES
    ('a-00000001-0000-4000-8000-000000000001', 'a1', 'u-00000000-0000-4000-a000-000000000001', 'DJI Mavic 3 Pro', 'DJI', 1500000, 'AVAILABLE', 'M3P-00123', NOW()),
    ('a-00000002-0000-4000-8000-000000000002', 'a1', 'u-00000000-0000-4000-a000-000000000002', 'DJI Inspire 3', 'DJI', 5000000, 'AVAILABLE', 'INS3-00456', NOW()),
    ('a-00000003-0000-4000-8000-000000000003', 'a2', 'u-00000000-0000-4000-a000-000000000001', 'DJI RS 3 Pro', 'DJI', 450000, 'AVAILABLE', 'RS3P-001', NOW()),
    ('a-00000004-0000-4000-8000-000000000004', 'a2', 'u-00000000-0000-4000-a000-000000000001', 'Zhiyun Crane 3S', 'Zhiyun', 350000, 'AVAILABLE', 'ZY-C3S-88', NOW()),
    ('a-00000005-0000-4000-8000-000000000005', 'a3', 'u-00000000-0000-4000-a000-000000000001', 'Sennheiser MKH 416', 'Sennheiser', 300000, 'RENTED', 'SN-416-09', NOW()),
    ('a-00000006-0000-4000-8000-000000000006', 'a3', 'u-00000000-0000-4000-a000-000000000002', 'Rode Wireless GO II', 'Rode', 150000, 'AVAILABLE', 'RD-WG2-11', NOW()),
    ('a-00000007-0000-4000-8000-000000000007', 'a4', 'u-00000000-0000-4000-a000-000000000001', 'Aputure LS 600d Pro', 'Aputure', 700000, 'AVAILABLE', 'AP-600D-PRO', NOW()),
    ('a-00000008-0000-4000-8000-000000000008', 'a4', 'u-00000000-0000-4000-a000-000000000002', 'Profoto B10X Plus', 'Profoto', 600000, 'RENTED', 'PR-B10XP', NOW()),
    ('a-00000009-0000-4000-8000-000000000009', 'a5', 'u-00000000-0000-4000-a000-000000000001', 'Canon EOS R5', 'Canon', 800000, 'AVAILABLE', 'R5-001239', NOW()),
    ('a-00000010-0000-4000-8000-000000000010', 'a5', 'u-00000000-0000-4000-a000-000000000001', 'Sony FE 24-70mm f/2.8 GM II', 'Sony', 400000, 'AVAILABLE', 'GM2-45211', NOW()),
    ('a-00000011-0000-4000-8000-000000000011', 'a5', 'u-00000000-0000-4000-a000-000000000002', 'Canon RF 70-200mm f/2.8L IS USM', 'Canon', 500000, 'AVAILABLE', 'RF72-1200', NOW()),
    ('a-00000012-0000-4000-8000-000000000012', 'a6', 'u-00000000-0000-4000-a000-000000000001', 'Manfrotto 055 Carbon Fiber', 'Manfrotto', 250000, 'AVAILABLE', 'MF-055CF-01', NOW()),
    ('a-00000013-0000-4000-8000-000000000013', 'a6', 'u-00000000-0000-4000-a000-000000000002', 'Peak Design Travel Tripod', 'Peak Design', 200000, 'AVAILABLE', 'PD-TT-007', NOW());

-- ============================================================================
-- 6. Chen hinh anh san pham
-- ============================================================================
INSERT INTO product_images (image_id, product_id, url, is_primary) VALUES
    ('pi-0001-0000-4000-8000-000000000001', 'p-00000001-0000-4000-8000-000000000001', 'https://cdn.vjshop.vn/may-anh/mirrorless/leica/leica-m11-p/leica-m11-p-6-1500x1500.jpg', true),
    ('pi-0002-0000-4000-8000-000000000002', 'p-00000001-0000-4000-8000-000000000001', 'https://img.vistek.net/prodimgalt/large/462808_1.jpg', false),
    ('pi-0003-0000-4000-8000-000000000003', 'p-00000002-0000-4000-8000-000000000002', 'https://camerawest.com/cdn/shop/files/1138710-02.jpg?v=1684775996', true),
    ('pi-0004-0000-4000-8000-000000000004', 'p-00000002-0000-4000-8000-000000000002', 'https://techland.com.vn/public_folder/folder_image/uploads/2023/05/may-anh-Leica-Q3-10.jpg', false),
    ('pi-0005-0000-4000-8000-000000000005', 'p-00000003-0000-4000-8000-000000000003', 'https://store-na.hasselblad.com/cdn/shop/files/X2D_692x692_4x_cecaf8af-26d4-461a-9eaa-7f90ea4647ab_1108x1108@2x.png?v=1753844475', true),
    ('pi-0006-0000-4000-8000-000000000006', 'p-00000003-0000-4000-8000-000000000003', 'https://cdn.vjshop.vn/tin-tuc/ra-mat-hasselblad-x2d-ii-100c/ra-mat-hasselblad-x2d-ii-100c-5.jpg', false),
    ('pi-0007-0000-4000-8000-000000000007', 'p-00000004-0000-4000-8000-000000000004', 'https://tokyocamera.vn/wp-content/uploads/2024/05/Ra-Mat-Fujifilm-GFX-100S-II-1.jpg', true),
    ('pi-0008-0000-4000-8000-000000000008', 'p-00000004-0000-4000-8000-000000000004', 'https://www.cined.com/content/uploads/2024/05/FUJIFILM-GFX100S-II-3-1300x750.jpg', false),
    ('pi-0009-0000-4000-8000-000000000009', 'p-00000005-0000-4000-8000-000000000005', 'https://pos.nvncdn.com/9af890-179840/ps/May-anh-Sony-A1-Chinh-hang.jpg?v=1775724585', true),
    ('pi-0010-0000-4000-8000-000000000010', 'p-00000005-0000-4000-8000-000000000005', 'https://bizweb.dktcdn.net/100/369/815/products/maxresdefault-d91fc2e7-be94-4ca7-bc6b-d7fea58cea12-fb09edf7-a9a0-43ec-bd80-d31517e270eb-95a39d54-ab0b-478d-9853-63b605ad8566.jpg?v=1653042104137', false),
    ('pi-0011-0000-4000-8000-000000000011', 'p-00000006-0000-4000-8000-000000000006', 'https://product.hstatic.net/200000409445/product/1_8a80fdd4576b4003a13a3e193eecaa10.jpg', true),
    ('pi-0012-0000-4000-8000-000000000012', 'p-00000006-0000-4000-8000-000000000006', 'https://product.hstatic.net/200000409445/product/2_c987bb6669c1427782a31de257722c01_master.jpg', false),
    ('pi-0013-0000-4000-8000-000000000013', 'p-00000007-0000-4000-8000-000000000007', 'https://zshop.vn/images/thumbnails/1357/1000/detailed/91/1635510652_IMG_1633119.jpg', true),
    ('pi-0014-0000-4000-8000-000000000014', 'p-00000007-0000-4000-8000-000000000007', 'https://imaging.nikon.com/imaging/lineup/mirrorless/z_9/img/product_01_02.jpg', false),
    ('pi-0015-0000-4000-8000-000000000015', 'p-00000008-0000-4000-8000-000000000008', 'https://cdn.vjshop.vn/may-anh/mirrorless/fujifilm/fujifilm-x-h2s/fujifilm-xh2s.jpg', true),
    ('pi-0016-0000-4000-8000-000000000016', 'p-00000008-0000-4000-8000-000000000008', 'https://images.squarespace-cdn.com/content/v1/545012a9e4b0988576f6b699/3fb5c1af-5df0-4835-83f2-ed3e915403be/fujifilm-xh2s-xf-33mm.jpg', false),
    ('pi-0017-0000-4000-8000-000000000017', 'p-00000009-0000-4000-8000-000000000009', 'https://static.insales-cdn.com/images/products/1/3069/875113469/Sony_Cyber-shot_DSC-RX100_VII__dscrx100M7_.jpg', true),
    ('pi-0018-0000-4000-8000-000000000018', 'p-00000009-0000-4000-8000-000000000009', 'https://amateurphotographer.com/wp-content/uploads/sites/7/2019/07/DSC01677-scaled.jpg', false),
    ('pi-0019-0000-4000-8000-000000000019', 'p-00000010-0000-4000-8000-000000000010', 'https://cdn.vjshop.vn/ong-kinh/mirrorless/sony/ong-kinh-sony-fe-24-70mm-f28-gm-ii/fe-24-70-mm-f28-gm-ii-00.jpg', true),
    ('pi-0020-0000-4000-8000-000000000020', 'p-00000010-0000-4000-8000-000000000010', 'https://zshop.vn/images/thumbnails/1357/1000/detailed/98/1651055463_IMG_1739520.jpg', false),
    ('pi-0021-0000-4000-8000-000000000021', 'p-00000011-0000-4000-8000-000000000011', 'https://cdn.vjshop.vn/ong-kinh/mirrorless/canon/canon-rf-70-200mm-f2-8l-is-usm/anh-mo-ta/ong-kinh-canon-rf-70-200mm-f28l-is-usm-0.jpg', true),
    ('pi-0022-0000-4000-8000-000000000022', 'p-00000011-0000-4000-8000-000000000011', 'https://cdn.vjshop.vn/ong-kinh/mirrorless/canon/canon-rf-70-200mm-f2-8l-is-usm/canon-rf-70-200mm-f28l-is-usm-01.jpg', false),
    ('pi-0023-0000-4000-8000-000000000023', 'p-00000012-0000-4000-8000-000000000012', 'https://product.hstatic.net/200000863343/product/balo_peak_design_everyday_den__20l__ver_1-basic_bcababc5ba554aa6971b9003a18daf31.jpg', true),
    ('pi-0024-0000-4000-8000-000000000024', 'p-00000012-0000-4000-8000-000000000012', 'https://pos.nvncdn.com/68fa8b-42431/pc/20190508_iCrCOHZhtn39uFMDRQakbfdJ.jpg?v=1673290662', false),
    ('pi-0025-0000-4000-8000-000000000025', 'p-00000013-0000-4000-8000-000000000013', 'https://progradedigital.com/wp-content/uploads/2022/06/CFexpressB_Card_165GB_1700_1500_2pack.jpg', true);

-- ============================================================================
-- 7. Chen hinh anh tai san
-- ============================================================================
INSERT INTO asset_images (image_id, asset_id, url, is_primary) VALUES
    ('ai-0001-0000-4000-8000-000000000001', 'a-00000001-0000-4000-8000-000000000001', 'https://image.anhducdigital.vn/nhiep-anh/drone/flycam/dji-mavic-3-pro/dji-mavic-3-pro-with-dji-rc-remote-chinh-hang-01.jpg', true),
    ('ai-0002-0000-4000-8000-000000000002', 'a-00000001-0000-4000-8000-000000000001', 'https://tokyocamera.vn/wp-content/uploads/2023/04/DJI-Mavic-3-Pro-Fly-More-Combo-DJI-RC.jpg', false),
    ('ai-0003-0000-4000-8000-000000000003', 'a-00000002-0000-4000-8000-000000000002', 'https://cdn.vjshop.vn/flycam/dji/dji-inspire-3/anh-sp/dji-inspire-3-7-2500x2500.jpg', true),
    ('ai-0004-0000-4000-8000-000000000004', 'a-00000002-0000-4000-8000-000000000002', 'https://www.dronivo.de/media/image/product/11815/lg/dji-inspire-3.jpg', false),
    ('ai-0005-0000-4000-8000-000000000005', 'a-00000003-0000-4000-8000-000000000003', 'https://tokyocamera.vn/wp-content/uploads/2023/01/DJI-RS-3-Mini-6-Tokyo-Camera.jpg', true),
    ('ai-0006-0000-4000-8000-000000000006', 'a-00000003-0000-4000-8000-000000000003', 'https://cdn.vjshop.vn/thiet-bi-lam-video/gimbal/dji/dji-rs-3/dji-rs-3-pro/dji-rs-3-pro-comnbo-2000x2000.jpg', false),
    ('ai-0007-0000-4000-8000-000000000007', 'a-00000004-0000-4000-8000-000000000004', 'https://product.hstatic.net/200000409445/product/6_ee56890ed2c643d080165ab26fe43a84.jpg', true),
    ('ai-0008-0000-4000-8000-000000000008', 'a-00000004-0000-4000-8000-000000000004', 'https://store.zhiyun-tech.com/cdn/shop/files/3S-aa5c3d75-84f9-4515-8d03-2878f6a7d85b-_6.jpg?v=1708584094&width=1780', false),
    ('ai-0009-0000-4000-8000-000000000009', 'a-00000005-0000-4000-8000-000000000005', 'https://www.adorama.com/images/Large/SEMKH416.JPG', true),
    ('ai-0010-0000-4000-8000-000000000010', 'a-00000005-0000-4000-8000-000000000005', 'https://cdn.uc.assets.prezly.com/b76cfabc-458a-4c75-bf6b-402f19d51530/-/crop/3287x1733/0,516/-/preview/-/resize/992/-/format/png/-/progressive/yes/-/quality/smart/', false),
    ('ai-0011-0000-4000-8000-000000000011', 'a-00000006-0000-4000-8000-000000000006', 'https://encrypted-tbn0.gstatic.com/images?q=tbn:ANd9GcQsYDPalJ4N4EtCLpEjUI-PxiyVraNlVmzFTg&s', true),
    ('ai-0012-0000-4000-8000-000000000012', 'a-00000006-0000-4000-8000-000000000006', 'https://pos.nvncdn.com/3d97f1-13296/ps/20231025_zWEfTKX73m.webp?v=1698232858', false),
    ('ai-0013-0000-4000-8000-000000000013', 'a-00000007-0000-4000-8000-000000000007', 'https://tokyocamera.vn/wp-content/uploads/2022/05/Aputure-LS-600d-Pro-Daylight-LED-Light-V-mount-1.jpg', true),
    ('ai-0014-0000-4000-8000-000000000014', 'a-00000007-0000-4000-8000-000000000007', 'https://m.media-amazon.com/images/I/714nDzpHSrL._AC_UF894,1000_QL80_.jpg', false),
    ('ai-0015-0000-4000-8000-000000000015', 'a-00000008-0000-4000-8000-000000000008', 'https://711srg4.711int.com/read/62068c61b2815?sig=t4xJxa0UejYfLiv3Rj0LxfHODIA%3D&pubKey=EsdjgASDerwdsgFGcesdaSDZQWER34SFPxE%25SFDGjhgsesdfg', true),
    ('ai-0016-0000-4000-8000-000000000016', 'a-00000008-0000-4000-8000-000000000008', 'https://www.bhphotovideo.com/images/fb/profoto_b10x_ocf_flash_head_1769136.jpg', false),
    ('ai-0017-0000-4000-8000-000000000017', 'a-00000009-0000-4000-8000-000000000009', 'https://cdn.vjshop.vn/may-anh/mirrorless/canon/canon-eos-r5-mark-ii/canon-eos-r5-mark-ii.jpg', true),
    ('ai-0018-0000-4000-8000-000000000018', 'a-00000009-0000-4000-8000-000000000009', 'https://www.cinemachine.com.au/cdn/shop/files/Untitleddesign_22_600x600_crop_center.png?v=1724826394', false),
    ('ai-0019-0000-4000-8000-000000000019', 'a-00000010-0000-4000-8000-000000000010', 'https://cdn.vjshop.vn/ong-kinh/mirrorless/sony/ong-kinh-sony-fe-24-70mm-f28-gm-ii/fe-24-70-mm-f28-gm-ii-00.jpg', true),
    ('ai-0020-0000-4000-8000-000000000020', 'a-00000010-0000-4000-8000-000000000010', 'https://zshop.vn/images/thumbnails/1357/1000/detailed/98/1651055463_IMG_1739520.jpg', false),
    ('ai-0021-0000-4000-8000-000000000021', 'a-00000011-0000-4000-8000-000000000011', 'https://cdn.vjshop.vn/ong-kinh/mirrorless/canon/canon-rf-70-200mm-f2-8l-is-usm/anh-mo-ta/ong-kinh-canon-rf-70-200mm-f28l-is-usm-0.jpg', true),
    ('ai-0022-0000-4000-8000-000000000022', 'a-00000011-0000-4000-8000-000000000011', 'https://cdn.vjshop.vn/ong-kinh/mirrorless/canon/canon-rf-70-200mm-f2-8l-is-usm/canon-rf-70-200mm-f28l-is-usm-01.jpg', false),
    ('ai-0023-0000-4000-8000-000000000023', 'a-00000012-0000-4000-8000-000000000012', 'https://pos.nvncdn.com/91002e-15402/ps/20241122_ErehNyCk4C.webp?v=1732272755', true),
    ('ai-0024-0000-4000-8000-000000000024', 'a-00000012-0000-4000-8000-000000000012', 'https://http2.mlstatic.com/D_Q_NP_2X_936622-MLA99455845460_112025-P.webp', false),
    ('ai-0025-0000-4000-8000-000000000025', 'a-00000013-0000-4000-8000-000000000013', 'https://zshop.vn/images/thumbnails/1357/1000/detailed/55/1563493561_1495136.jpg', true),
    ('ai-0026-0000-4000-8000-000000000026', 'a-00000013-0000-4000-8000-000000000013', 'https://giangduydat.vn/product/chan-peakdesign-travel-tripod.jpg', false);
