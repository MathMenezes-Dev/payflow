
package com.payflow.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

public class DepositRequest {

    @NotNull(message = "O valor do depósito é obrigatório")
    @DecimalMin(value = "0.00", inclusive = false, message = "O valor do depósito deve ser maior que zero")
    private BigDecimal amount;

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}
