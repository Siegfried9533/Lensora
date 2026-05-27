package com.camerashop.service;

import com.camerashop.dto.ProductDTO;
import com.camerashop.entity.Product;
import com.camerashop.entity.ProductImage;
import com.camerashop.repository.ProductRepository;
import com.camerashop.repository.ProductImageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductImageRepository productImageRepository;

    public Page<ProductDTO> getAllProducts(Pageable pageable) {
        return productRepository.findAll(pageable).map(this::toDTO);
    }

    public Page<ProductDTO> getProductsByCategory(String categoryId, Pageable pageable) {
        return productRepository.findByCategoryId(categoryId, pageable).map(this::toDTO);
    }

    public Page<ProductDTO> searchProducts(String searchQuery, String categoryId, Pageable pageable) {
        String normalizedSearch = normalizeFilter(searchQuery);
        String normalizedCategoryId = normalizeFilter(categoryId);

        if (normalizedSearch == null && normalizedCategoryId == null) {
            return productRepository.findAll(pageable).map(this::toDTO);
        }

        if (normalizedSearch == null) {
            return productRepository.findByCategoryId(normalizedCategoryId, pageable).map(this::toDTO);
        }

        if (normalizedCategoryId == null) {
            return productRepository.findByProductNameContainingIgnoreCase(normalizedSearch, pageable).map(this::toDTO);
        }

        return productRepository
                .findByProductNameContainingIgnoreCaseAndCategory_CategoryId(
                        normalizedSearch,
                        normalizedCategoryId,
                        pageable)
                .map(this::toDTO);
    }

    public ProductDTO getProductById(String id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));
        return toDTO(product);
    }

    public List<ProductDTO> getProductsByUser(String userId) {
        return productRepository.findByUserId(userId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    private String normalizeFilter(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private ProductDTO toDTO(Product product) {
        List<ProductImage> images = productImageRepository.findByProductId(product.getProductId());
        String primaryImageUrl = images.stream()
                .filter(ProductImage::getIsPrimary)
                .findFirst()
                .map(ProductImage::getUrl)
                .orElse("https://via.placeholder.com/800");

        List<String> imageUrls = images.stream()
                .map(ProductImage::getUrl)
                .collect(Collectors.toList());

        return ProductDTO.builder()
                .productId(product.getProductId())
                .categoryId(product.getCategory().getCategoryId())
                .categoryName(product.getCategory().getCategoryName())
                .userId(product.getUser().getUserId())
                .productName(product.getProductName())
                .brand(product.getBrand())
                .description(product.getDescription())
                .price(product.getPrice())
                .stockQuantity(product.getStockQuantity())
                .imageUrls(imageUrls)
                .primaryImageUrl(primaryImageUrl)
                .build();
    }
}
