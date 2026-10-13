
package com.payflow.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

public class TransferRequest {

    @NotNull(message = "O ID do remetente é obrigatório")
    private Long senderId;

    @NotNull(message = "O ID do destinatário é obrigatório")
    private Long receiverId;

    @NotNull(message = "O valor da transferência é obrigatório")
    @DecimalMin(
            value = "0.00",
            inclusive = false,
            message = "O valor da transferência deve ser maior que zero"
    )
    private BigDecimal amount;

    public Long getSenderId() {
        return senderId;
    }

    public void setSenderId(Long senderId) {
        this.senderId = senderId;
    }

    public Long getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(Long receiverId) {
        this.receiverId = receiverId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}
