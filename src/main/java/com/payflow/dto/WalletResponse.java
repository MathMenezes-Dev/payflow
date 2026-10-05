package com.payflow.dto;

import java.math.BigDecimal;

public class WalletResponse {
	
	private Long userId;
	private BigDecimal balance;
	
	public WalletResponse(Long userId, BigDecimal balance) {
		this.userId = userId;
		this.balance = balance;
		
	}
	
	public Long getUserId() {
		return userId;
	}
	
	public BigDecimal getBalance() {
		return balance;
	}

}
