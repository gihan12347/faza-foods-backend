package com.fasa.orders.controller;

import com.fasa.orders.dto.ProductDto;
import com.fasa.orders.dto.ProductForm;
import com.fasa.orders.entity.ProductEntity;
import com.fasa.orders.service.ProductService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/products")
public class ProductAdminController {

    private final ProductService productService;

    public ProductAdminController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public String listProducts(Model model) {
        List<ProductEntity> products = productService.findAllEntities();
        model.addAttribute("sidebarActive", "products");
        model.addAttribute("products", products);
        model.addAttribute("suggestedNextId", productService.suggestNextId());
        model.addAttribute("lowStockCount", productService.countLowStockBelowMinimum());

        if (!model.containsAttribute("productForm")) {
            model.addAttribute("productForm", ProductForm.empty(productService.suggestNextId()));
        }
        return "products";
    }

    @GetMapping("/new")
    public String legacyNewRedirect() {
        return "redirect:/products";
    }

    @GetMapping("/{id}/edit")
    public String legacyEditRedirect(@PathVariable Long id) {
        return "redirect:/products";
    }

    @GetMapping("/{id}/data")
    @ResponseBody
    public ResponseEntity<ProductForm> productFormData(@PathVariable Long id) {
        return productService.findDtoById(id)
                .map(dto -> ResponseEntity.ok(ProductForm.fromDto(dto)))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public String createProduct(
            @ModelAttribute ProductForm productForm,
            RedirectAttributes redirectAttributes) {
        try {
            ProductDto saved = productService.create(productForm.toDto());
            redirectAttributes.addFlashAttribute("flashMessage",
                    "Product \"" + saved.getName() + "\" created (ID " + saved.getId() + ").");
            return "redirect:/products";
        } catch (IllegalArgumentException ex) {
            return redirectFormError(productForm, "create", ex.getMessage(), redirectAttributes);
        }
    }

    @PostMapping("/{id}")
    public String updateProduct(
            @PathVariable Long id,
            @ModelAttribute ProductForm productForm,
            RedirectAttributes redirectAttributes) {
        try {
            productForm.setId(id);
            ProductDto saved = productService.update(id, productForm.toDto());
            redirectAttributes.addFlashAttribute("flashMessage",
                    "Product \"" + saved.getName() + "\" updated.");
            return "redirect:/products";
        } catch (IllegalArgumentException ex) {
            productForm.setId(id);
            return redirectFormError(productForm, "edit", ex.getMessage(), redirectAttributes);
        }
    }

    @PostMapping("/{id}/delete")
    public String deleteProduct(
            @PathVariable Long id,
            @RequestParam(required = false) String name,
            RedirectAttributes redirectAttributes) {
        try {
            productService.delete(id);
            String label = name != null && !name.trim().isEmpty() ? name.trim() : ("ID " + id);
            redirectAttributes.addFlashAttribute("flashMessage", "Product \"" + label + "\" deleted.");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("flashError", ex.getMessage());
        }
        return "redirect:/products";
    }

    private String redirectFormError(
            ProductForm productForm,
            String formMode,
            String errorMessage,
            RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("flashError", errorMessage);
        redirectAttributes.addFlashAttribute("openModal", "form");
        redirectAttributes.addFlashAttribute("formMode", formMode);
        redirectAttributes.addFlashAttribute("productForm", productForm);
        return "redirect:/products";
    }

}
