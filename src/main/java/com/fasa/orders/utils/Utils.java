package com.fasa.orders.utils;

import com.fasa.orders.enums.DeliveryTypes;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {

    private static final Pattern WEIGHT_PATTERN =
            Pattern.compile("^(\\d+(?:\\.\\d+)?)\\s*(kg|g|ml)$");

    public static Double parseWeightToKg(String weightValue) {
        if (weightValue == null) {
            return (double) 0;
        }
        String normalized = weightValue.trim().toLowerCase();
        Matcher matcher = WEIGHT_PATTERN.matcher(normalized);
        if (!matcher.matches()) {
            return (double) 0;
        }
        double value;
        try {
            value = Double.parseDouble(matcher.group(1));
        } catch (NumberFormatException e) {
            return (double) 0;
        }
        if (value <= 0) {
            return (double) 0;
        }
        String unit = matcher.group(2);
        return "kg".equals(unit) ? value : value / 1000.0;
    }

    public static DeliveryTypes deliveryTypeFromString(String value) {
        if (value == null) {
            return DeliveryTypes.cash_on_delivery;
        }
        try {
            return DeliveryTypes.valueOf(value.toLowerCase());
        } catch (IllegalArgumentException ex) {
            return DeliveryTypes.cash_on_delivery;
        }
    }
}
