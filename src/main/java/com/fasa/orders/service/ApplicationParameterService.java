package com.fasa.orders.service;

import com.fasa.orders.controller.OrderController;
import com.fasa.orders.entity.ApplicationParameterEntity;
import com.fasa.orders.repository.ApplicationParameterRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ApplicationParameterService {
    private static final Logger log = LoggerFactory.getLogger(ApplicationParameterService.class);

    private static final String KEY_BANK_ACCOUNT_NAME = "fasa.orders.bank.account-name";
    private static final String KEY_BANK_ACCOUNT_NUMBER = "fasa.orders.bank.account-number";
    private static final String KEY_BANK_LABEL = "fasa.orders.bank.bank-label";
    private static final String KEY_BANK_BRANCH = "fasa.orders.bank.branch";
    private static final String STORE_FRONT_BASE_URL = "fasa.orders.storefront.base-url";
    private static final String STORE_PUBLIC_BASE_URL = "fasa.orders.api.public-base-url";
    private static final String WHATSAPP_DISPLAY_NUMBER = "fasa.orders.whatsapp.display";
    private static final String WHATSAPP_PHONE_NUMBER = "fasa.orders.whatsapp.phone";

    private final ApplicationParameterRepository applicationParameterRepository;

    public ApplicationParameterService(ApplicationParameterRepository repository) {
        this.applicationParameterRepository = repository;
    }

    public String getBankAccountName() {
        return resolve(KEY_BANK_ACCOUNT_NAME);
    }

    public String getBankAccountNumber() {
        return resolve(KEY_BANK_ACCOUNT_NUMBER);
    }

    public String getBankLabel() {
        return resolve(KEY_BANK_LABEL);
    }

    public String getBankBranch() {
        return resolve(KEY_BANK_BRANCH);
    }

    public String getStorePublicBaseUrl() { return resolve(STORE_PUBLIC_BASE_URL); }

    public String getStoreFrontBaseUrl() { return resolve(STORE_FRONT_BASE_URL); }

    public String getWhatsappDisplayNumber() { return resolve(WHATSAPP_DISPLAY_NUMBER); }

    public String getWhatsappPhoneNumber() { return resolve(WHATSAPP_PHONE_NUMBER); }

    @Cacheable(value = "application_parameter", key = "#key")
    public String resolve(String key) {
        log.info("=== Get Data from Database ==");
        Optional<ApplicationParameterEntity> applicationParameter = applicationParameterRepository.findById(key);
        if (applicationParameter.isPresent()) {
            return applicationParameter.get().getParamValue();
        }
        return "";
    }
}
