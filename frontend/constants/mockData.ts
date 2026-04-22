import { User, Category, Product, Asset, ImageRecord, Rental, Order } from './types';

export const USERS: User[] = [
  {
    userId: 'u1',
    userName: 'testuser',
    email: 'test@example.com',
    password: 'password123',
    role: 'user',
    trustScore: 85,
  },
  {
    userId: 'u2',
    userName: 'johndoe',
    email: 'john@example.com',
    password: 'password123',
    role: 'admin',
    trustScore: 99,
  }
];

export const CATEGORIES: Category[] = [
  { categoryId: 'c1', categoryName: 'Premium Camera', type: 'PRODUCT' },
  { categoryId: 'c2', categoryName: 'Medium Format', type: 'PRODUCT' },
  { categoryId: 'c3', categoryName: 'Mirrorless', type: 'PRODUCT' },
  { categoryId: 'c7', categoryName: 'Action Camera', type: 'PRODUCT' },
  { categoryId: 'c8', categoryName: 'Drone', type: 'PRODUCT' },
  { categoryId: 'c9', categoryName: 'Accessories', type: 'PRODUCT' },
  { categoryId: 'c4', categoryName: 'Camera Body', type: 'ASSET' },
  { categoryId: 'c5', categoryName: 'Lens', type: 'ASSET' },
  { categoryId: 'c6', categoryName: 'Lighting', type: 'ASSET' },
  { categoryId: 'c10', categoryName: 'Stabilizer', type: 'ASSET' },
  { categoryId: 'c11', categoryName: 'Audio', type: 'ASSET' }
];

export const PRODUCTS: Product[] = [
  { productId: 'p1', categoryId: 'c1', productName: 'LEICA M11', brand: 'Leica', description: 'The newest digital rangefinder from Leica combining classic design with contemporary technology.', price: 199000000, stockQuantity: 5 },
  { productId: 'p2', categoryId: 'c2', productName: 'HASSELBLAD X2D', brand: 'Hasselblad', description: '100-megapixel medium format mirrorless camera for ultimate image quality.', price: 175000000, stockQuantity: 2 },
  { productId: 'p3', categoryId: 'c3', productName: 'SONY α1', brand: 'Sony', description: 'The one. Flagship full-frame mirrorless camera with 50.1MP and 30fps shooting.', price: 145000000, stockQuantity: 10 },
  { productId: 'p4', categoryId: 'c3', productName: 'CANON EOS R3', brand: 'Canon', description: 'High performance sports and wildlife mirrorless camera.', price: 135000000, stockQuantity: 4 },
  { productId: 'p5', categoryId: 'c3', productName: 'NIKON Z9', brand: 'Nikon', description: 'Professional full-frame mirrorless without mechanical shutter.', price: 125000000, stockQuantity: 7 },
  { productId: 'p6', categoryId: 'c3', productName: 'FUJIFILM X-H2S', brand: 'Fujifilm', description: 'High speed APS-C camera with stacked sensor technology.', price: 58000000, stockQuantity: 15 },
  { productId: 'p7', categoryId: 'c7', productName: 'GoPro HERO 12 Black', brand: 'GoPro', description: 'The ultimate action camera for extreme sports.', price: 9500000, stockQuantity: 50 },
  { productId: 'p8', categoryId: 'c7', productName: 'DJI Osmo Action 4', brand: 'DJI', description: 'Excellent low light action cam.', price: 8500000, stockQuantity: 45 },
  { productId: 'p9', categoryId: 'c8', productName: 'DJI Mavic 3 Pro', brand: 'DJI', description: 'Triple camera drone for professional cinematic shots.', price: 52000000, stockQuantity: 8 },
  { productId: 'p10', categoryId: 'c8', productName: 'DJI Mini 4 Pro', brand: 'DJI', description: 'Mini camera drone under 249g with omnidirectional obstacle sensing.', price: 23000000, stockQuantity: 20 },
  { productId: 'p11', categoryId: 'c9', productName: 'Peak Design Everyday Backpack', brand: 'Peak Design', description: 'Award winning camera bags for everyday carry.', price: 6500000, stockQuantity: 30 },
  { productId: 'p12', categoryId: 'c9', productName: 'ProGrade CFexpress Type B 512GB', brand: 'ProGrade', description: 'Ultra fast memory card for 8K video.', price: 12500000, stockQuantity: 12 }
];

export const ASSETS: Asset[] = [
  { assetId: 'a1', categoryId: 'c4', modelName: 'Canon EOS R5', brand: 'Canon', dailyRate: 800000, status: 'AVAILABLE', seriNumber: 'R5-001239' },
  { assetId: 'a2', categoryId: 'c5', modelName: 'Sony FE 24-70mm f/2.8 GM II', brand: 'Sony', dailyRate: 400000, status: 'AVAILABLE', seriNumber: 'GM2-45211' },
  { assetId: 'a3', categoryId: 'c4', modelName: 'RED Komodo 6K', brand: 'RED', dailyRate: 2500000, status: 'RENTED', seriNumber: 'KMD-99120' },
  { assetId: 'a4', categoryId: 'c4', modelName: 'ARRI Alexa Mini LF', brand: 'ARRI', dailyRate: 8000000, status: 'AVAILABLE', seriNumber: 'ALX-10332' },
  { assetId: 'a5', categoryId: 'c5', modelName: 'Canon RF 70-200mm f/2.8', brand: 'Canon', dailyRate: 500000, status: 'AVAILABLE', seriNumber: 'RF72-1200' },
  { assetId: 'a6', categoryId: 'c5', modelName: 'Sigma Art 35mm f/1.4', brand: 'Sigma', dailyRate: 200000, status: 'AVAILABLE', seriNumber: 'SG-ART35-1' },
  { assetId: 'a7', categoryId: 'c6', modelName: 'Aputure LS 600d Pro', brand: 'Aputure', dailyRate: 700000, status: 'AVAILABLE', seriNumber: 'AP-600D-PRO' },
  { assetId: 'a8', categoryId: 'c6', modelName: 'Profoto B10X Plus', brand: 'Profoto', dailyRate: 600000, status: 'RENTED', seriNumber: 'PR-B10XP' },
  { assetId: 'a9', categoryId: 'c10', modelName: 'DJI RS 3 Pro Gimbal', brand: 'DJI', dailyRate: 450000, status: 'AVAILABLE', seriNumber: 'RS3P-001' },
  { assetId: 'a10', categoryId: 'c10', modelName: 'Zhiyun Crane 3S', brand: 'Zhiyun', dailyRate: 350000, status: 'AVAILABLE', seriNumber: 'ZY-C3S-88' },
  { assetId: 'a11', categoryId: 'c11', modelName: 'Sennheiser MKH 416', brand: 'Sennheiser', dailyRate: 300000, status: 'RENTED', seriNumber: 'SN-416-09' },
  { assetId: 'a12', categoryId: 'c11', modelName: 'Rode Wireless GO II', brand: 'Rode', dailyRate: 150000, status: 'AVAILABLE', seriNumber: 'RD-WG2-11' }
];

const PHOTO_POOL = [
  'https://images.unsplash.com/photo-1725779318629-eda3e096eb86?w=800',
  'https://images.unsplash.com/photo-1511140973288-19bf21d7e771?w=800',
  'https://images.unsplash.com/photo-1585548601784-e319505354bb?w=800',
  'https://images.unsplash.com/photo-1516035069371-29a1b244cc32?w=800',
  'https://images.unsplash.com/photo-1516961642265-531546e84af2?w=800',
  'https://images.unsplash.com/photo-1617005082833-18e8093153e7?w=800',
  'https://images.unsplash.com/photo-1502920917128-1aa500764cbd?w=800',
  'https://images.unsplash.com/photo-1560064278-65127ee6aa25?w=800',
  'https://images.unsplash.com/photo-1493770348161-369560ae357d?w=800',
  'https://images.unsplash.com/photo-1514316454349-750a7fd3da3a?w=800',
  'https://images.unsplash.com/photo-1452423924765-680fa2a9121a?w=800',
  'https://images.unsplash.com/photo-1520390138845-fd2d229dd553?w=800'
];

export const IMAGES: ImageRecord[] = [
  ...PRODUCTS.map((p, index) => ({
    imageId: `img_p_${p.productId}`,
    entityId: p.productId,
    url: PHOTO_POOL[index % PHOTO_POOL.length],
    isPrimary: true,
    type: 'PRODUCT' as const
  })),
  ...ASSETS.map((a, index) => ({
    imageId: `img_a_${a.assetId}`,
    entityId: a.assetId,
    url: PHOTO_POOL[(index + 3) % PHOTO_POOL.length], // offset to mix them up
    isPrimary: true,
    type: 'ASSET' as const
  }))
];


export const RENTALS: Rental[] = [
  {
    rentalId: 'r1',
    userId: 'u1',
    assetId: 'a3',
    startDate: '2026-04-05T00:00:00Z',
    endDate: '2026-04-10T00:00:00Z',
    depositFee: 30000000,
    totalRentFee: 12500000,
    penaltyFee: 0,
    status: 'ACTIVE'
  },
  {
    rentalId: 'r2',
    userId: 'u1',
    assetId: 'a8',
    startDate: '2026-04-01T00:00:00Z',
    endDate: '2026-04-08T00:00:00Z',
    depositFee: 15000000,
    totalRentFee: 4200000,
    penaltyFee: 0,
    status: 'ACTIVE'
  }
];

export const ORDERS: Order[] = [
  {
    orderId: 'o1',
    userId: 'u1',
    orderDate: '2026-04-01T10:00:00Z',
    totalAmount: 145000000,
    shippingAddress: '123 Photography Lane, Camera City',
    status: 'DELIVERED'
  },
  {
    orderId: 'o2',
    userId: 'u1',
    orderDate: '2026-04-06T14:30:00Z',
    totalAmount: 18000000,
    shippingAddress: '123 Photography Lane, Camera City',
    status: 'PENDING'
  }
];

// Để dễ gọi sau này, viết 1 số helper methods nhỏ:
export const getPrimaryImage = (entityId: string, type: 'PRODUCT' | 'ASSET') => {
  return IMAGES.find(i => i.entityId === entityId && i.type === type && i.isPrimary)?.url || 'https://via.placeholder.com/800';
};
