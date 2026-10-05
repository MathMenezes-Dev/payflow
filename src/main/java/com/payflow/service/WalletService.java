package com.payflow.service;

import org.springframework.stereotype.Service;

import com.payflow.dto.WalletResponse;
import com.payflow.entity.Wallet;
import com.payflow.repository.WalletRepository;

@Service
public class WalletService {

	private final WalletRepository walletRepository;

	public WalletService(WalletRepository walletRepository) {
	    this.walletRepository = walletRepository;
	}

	public WalletResponse findByUserId(Long userId) {

	    Wallet wallet = walletRepository.findByUserId(userId)
	            .orElseThrow(() -> new IllegalArgumentException("Carteira não encontrada"));

	    return new WalletResponse(
	            wallet.getUser().getId(),
	            wallet.getBalance()
	    );
	}
	
}