package com.backend.givr.shared.dtos;

import com.backend.givr.shared.enums.CurrencyCode;
import com.backend.givr.shared.enums.PaymentMethod;
import jakarta.validation.constraints.Email;

import java.math.BigDecimal;
import java.util.List;

public record InitTransaction(BigDecimal amount, String customerName,
                              @Email String customerEmail, String paymentReference, String paymentDescription, CurrencyCode currencyCode,
                              String redirectUrl, List<PaymentMethod> paymentMethods) {

}
