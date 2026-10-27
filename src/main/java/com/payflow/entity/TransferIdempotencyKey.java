package com.payflow.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "transfer_idempotency_key")
@Getter
@NoArgsConstructor

public class TransferIdempotencyKey {
	
	@Id
	@Column(name = "idempotency_key", length = 100)
	private String idempotencyKey;
	
	public TransferIdempotencyKey(String idempotencyKey) {
		this.idempotencyKey = idempotencyKey;
		
	}
	
}
