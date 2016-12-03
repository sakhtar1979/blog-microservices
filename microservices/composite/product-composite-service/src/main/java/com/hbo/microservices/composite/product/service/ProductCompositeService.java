package com.hbo.microservices.composite.product.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import com.hbo.microservices.composite.product.model.ProductAggregated;
import com.hbo.microservices.model.Product;
import com.hbo.microservices.model.Recommendation;
import com.hbo.microservices.model.Review;
import com.hbo.microservices.util.ServiceUtils;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static java.util.concurrent.CompletableFuture.supplyAsync;
import static java.util.concurrent.CompletableFuture.allOf;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;



@Produces(APPLICATION_JSON)
@Consumes(APPLICATION_JSON)
@RestController
public class ProductCompositeService {

    private static final Logger LOG = LoggerFactory.getLogger(ProductCompositeService.class);

    @Autowired
    ProductCompositeIntegration integration;

    @Autowired
    ServiceUtils util;

    @GetMapping("/")
    public String getProduct() {
        return "{\"timestamp\":\"" + new Date() + "\",\"content\":\"Hello from ProductAPi\"}";
    }

    @GetMapping("/{productId}")
    public ResponseEntity<ProductAggregated> getProduct(@PathVariable int productId) {

        LOG.info("Synch start...");

        // 1. First get mandatory product information
        Product product = getBasicProductInfo(productId);

        // 2. Get optional recommendations
        List<Recommendation> recommendations = getRecommendations(productId);

        // 3. Get optional reviews
        List<Review> reviews = getReviews(productId);

        return util.createOkResponse(new ProductAggregated(product, recommendations, reviews));
    }

//    @GetMapping("/{productId}")
    public ResponseEntity<ProductAggregated> getProductAsync(@PathVariable int productId) {

        try {
            LOG.info("Asynch start...");
            CompletableFuture<Product>              productFuture            = supplyAsync( () -> getBasicProductInfo(productId));
            CompletableFuture<List<Recommendation>> recommendationListFuture = supplyAsync( () -> getRecommendations(productId));
            CompletableFuture<List<Review>>         reviewListFuture         = supplyAsync( () -> getReviews(productId));

            LOG.info("Asynch, allOf.join...");
            allOf(productFuture, recommendationListFuture, reviewListFuture).join();


            LOG.info("Asynch, create result and return...");
            return util.createOkResponse(new ProductAggregated(productFuture.get(), recommendationListFuture.get(), reviewListFuture.get()));

        } catch (InterruptedException | ExecutionException e) {
            LOG.error("getProductAsync error", e);
            throw new RuntimeException(e);
        }
    }

    private Product getBasicProductInfo(@PathVariable int productId) {
        ResponseEntity<Product> productResult = integration.getProduct(productId);
        Product product = null;
        if (!productResult.getStatusCode().is2xxSuccessful()) {
            // Something went wrong with getProduct, simply skip the product-information in the response
            LOG.debug("Call to getProduct failed: {}", productResult.getStatusCode());
        } else {
            product = productResult.getBody();
        }
        return product;
    }

    private List<Review> getReviews(@PathVariable int productId) {
        ResponseEntity<List<Review>> reviewsResult = integration.getReviews(productId);
        List<Review> reviews = null;
        if (!reviewsResult.getStatusCode().is2xxSuccessful()) {
            // Something went wrong with getReviews, simply skip the review-information in the response
            LOG.debug("Call to getReviews failed: {}", reviewsResult.getStatusCode());
        } else {
            reviews = reviewsResult.getBody();
        }
        return reviews;
    }

    private List<Recommendation> getRecommendations(@PathVariable int productId) {
        List<Recommendation> recommendations = null;
        try {
            ResponseEntity<List<Recommendation>> recommendationResult = integration.getRecommendations(productId);
            if (!recommendationResult.getStatusCode().is2xxSuccessful()) {
                // Something went wrong with getRecommendations, simply skip the recommendation-information in the response
                LOG.debug("Call to getRecommendations failed: {}", recommendationResult.getStatusCode());
            } else {
                recommendations = recommendationResult.getBody();
            }
        } catch (Throwable t) {
            LOG.error("getProduct error", t);
            throw t;
        }
        return recommendations;
    }
}
