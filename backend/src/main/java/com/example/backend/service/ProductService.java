package com.example.backend.service;

import com.example.backend.dto.ProductRequest;
import com.example.backend.dto.ProductResponse;
import com.example.backend.entity.Category;
import com.example.backend.entity.Image;
import com.example.backend.entity.Product;
import com.example.backend.repository.CategoryRepository;
import com.example.backend.repository.ImageRepository;
import com.example.backend.repository.ProductRepository;
import jakarta.transaction.Transactional;
import java.util.List;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;
    private final ImageRepository imageRepository;
    private final CategoryRepository categoryRepository;

    @Transactional
    public ProductResponse createProduct(ProductRequest request) {
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + request.getCategoryId()));

        Product product = new Product();
        product.setCategory(category);
        product.setProductName(request.getProductName());
        product.setBrand(request.getBrand());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setStockQuantity(request.getStockQuantity());

        Product savedProduct = productRepository.save(product);

        if (request.getImageUrls() != null && !request.getImageUrls().isEmpty()) {
            for (String url : request.getImageUrls()) {
                Image img = new Image();
                img.setUrl(url);
                img.setEntityId(savedProduct.getProductId());
                img.setType("PRODUCT");
                imageRepository.save(img);
            }
        }

        return getProductById(savedProduct.getProductId());
    }

    public ProductResponse getProductById(@NonNull Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Khong tim thay san pham voi ID: " + id));

        List<String> imageUrls = imageRepository.findByEntityIdAndType(id, "PRODUCT")
                .stream()
                .map(Image::getUrl)
                .toList();

        return ProductResponse.builder()
                .productId(product.getProductId())
                .categoryName(product.getCategory().getCategoryName())
                .productName(product.getProductName())
                .brand(product.getBrand())
                .description(product.getDescription())
                .price(product.getPrice())
                .stockQuantity(product.getStockQuantity())
                .imageUrls(imageUrls)
                .build();
    }

    public List<ProductResponse> getAllProducts() {
        return productRepository.findAll().stream().map(product -> {
            List<String> imageUrls = imageRepository.findByEntityIdAndType(product.getProductId(), "PRODUCT")
                    .stream()
                    .map(Image::getUrl)
                    .toList();

            return ProductResponse.builder()
                    .productId(product.getProductId())
                    .categoryName(product.getCategory().getCategoryName())
                    .productName(product.getProductName())
                    .brand(product.getBrand())
                    .description(product.getDescription())
                    .price(product.getPrice())
                    .stockQuantity(product.getStockQuantity())
                    .imageUrls(imageUrls)
                    .build();
        }).toList();
    }
}