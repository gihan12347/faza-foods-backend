package com.fasa.orders.controller;

import com.fasa.orders.dto.ShippingRateForm;
import com.fasa.orders.entity.ShippingRateTiers;
import com.fasa.orders.enums.DeliveryTypes;
import com.fasa.orders.enums.RateTypes;
import com.fasa.orders.service.ShippingRateService;
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
@RequestMapping("/shipping-rates")
public class ShippingRateAdminController {

    private final ShippingRateService shippingRateService;

    public ShippingRateAdminController(ShippingRateService shippingRateService) {
        this.shippingRateService = shippingRateService;
    }

    @GetMapping
    public String list(Model model) {
        List<ShippingRateTiers> rates = shippingRateService.findAll();
        model.addAttribute("sidebarActive", "shipping-rates");
        model.addAttribute("rates", rates);
        model.addAttribute("suggestedNextId", shippingRateService.suggestNextId());
        model.addAttribute("rateTypes", RateTypes.values());
        model.addAttribute("deliveryTypes", DeliveryTypes.values());

        if (!model.containsAttribute("shippingRateForm")) {
            model.addAttribute("shippingRateForm", ShippingRateForm.empty(shippingRateService.suggestNextId()));
        }
        return "shipping-rates";
    }

    @GetMapping("/{id}/data")
    @ResponseBody
    public ResponseEntity<ShippingRateForm> formData(@PathVariable Long id) {
        return shippingRateService.findFormById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public String create(
            @ModelAttribute ShippingRateForm shippingRateForm,
            RedirectAttributes redirectAttributes) {
        try {
            ShippingRateTiers saved = shippingRateService.create(shippingRateForm);
            redirectAttributes.addFlashAttribute("flashMessage",
                    "Shipping rate tier #" + saved.getId() + " created.");
            return "redirect:/shipping-rates";
        } catch (IllegalArgumentException ex) {
            return redirectFormError(shippingRateForm, "create", ex.getMessage(), redirectAttributes);
        }
    }

    @PostMapping("/{id}")
    public String update(
            @PathVariable Long id,
            @ModelAttribute ShippingRateForm shippingRateForm,
            RedirectAttributes redirectAttributes) {
        try {
            shippingRateForm.setId(id);
            ShippingRateTiers saved = shippingRateService.update(id, shippingRateForm);
            redirectAttributes.addFlashAttribute("flashMessage",
                    "Shipping rate tier #" + saved.getId() + " updated.");
            return "redirect:/shipping-rates";
        } catch (IllegalArgumentException ex) {
            shippingRateForm.setId(id);
            return redirectFormError(shippingRateForm, "edit", ex.getMessage(), redirectAttributes);
        }
    }

    @PostMapping("/{id}/delete")
    public String delete(
            @PathVariable Long id,
            @RequestParam(required = false) String label,
            RedirectAttributes redirectAttributes) {
        try {
            shippingRateService.delete(id);
            String text = label != null && !label.trim().isEmpty() ? label.trim() : ("#" + id);
            redirectAttributes.addFlashAttribute("flashMessage", "Shipping rate " + text + " deleted.");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("flashError", ex.getMessage());
        }
        return "redirect:/shipping-rates";
    }

    private String redirectFormError(
            ShippingRateForm shippingRateForm,
            String formMode,
            String errorMessage,
            RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("flashError", errorMessage);
        redirectAttributes.addFlashAttribute("openModal", "form");
        redirectAttributes.addFlashAttribute("formMode", formMode);
        redirectAttributes.addFlashAttribute("shippingRateForm", shippingRateForm);
        return "redirect:/shipping-rates";
    }
}
