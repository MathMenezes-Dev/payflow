
package com.payflow.service;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Transactional
    public WalletResponse deposit(Long userId, BigDecimal amount) {

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("O valor do depósito deve ser maior que zero");
        }

        Wallet wallet = walletRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Carteira não encontrada"));

        BigDecimal newBalance = wallet.getBalance().add(amount);

        wallet.setBalance(newBalance);

        Wallet savedWallet = walletRepository.save(wallet);

        return new WalletResponse(
                savedWallet.getUser().getId(),
                savedWallet.getBalance()
        );
    }
}
