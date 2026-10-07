package com.payflow.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.payflow.dto.TransferRequest;
import com.payflow.entity.Transfer;
import com.payflow.entity.Wallet;
import com.payflow.repository.TransferRepository;
import com.payflow.repository.WalletRepository;

@Service
public class TransferService {

    private final TransferRepository transferRepository;
    private final WalletRepository walletRepository;

    public TransferService(
            TransferRepository transferRepository,
            WalletRepository walletRepository) {
        this.transferRepository = transferRepository;
        this.walletRepository = walletRepository;
    }

    @Transactional
    public Transfer transfer(TransferRequest request) {

        if (request.getSenderId().equals(request.getReceiverId())) {
            throw new IllegalArgumentException("Origem e destino devem ser diferentes");
        }

        if (request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("O valor da transferência deve ser maior que zero");
        }

        Wallet senderWallet = walletRepository.findByUserId(request.getSenderId())
                .orElseThrow(() -> new IllegalArgumentException("Carteira do remetente não encontrada"));

        Wallet receiverWallet = walletRepository.findByUserId(request.getReceiverId())
                .orElseThrow(() -> new IllegalArgumentException("Carteira do destinatário não encontrada"));

        if (senderWallet.getBalance().compareTo(request.getAmount()) < 0) {
            throw new IllegalArgumentException("Saldo insuficiente");
        }

        senderWallet.setBalance(
                senderWallet.getBalance().subtract(request.getAmount())
        );

        receiverWallet.setBalance(
                receiverWallet.getBalance().add(request.getAmount())
        );

        walletRepository.save(senderWallet);
        walletRepository.save(receiverWallet);

        Transfer transfer = new Transfer();

        transfer.setSenderId(request.getSenderId());
        transfer.setReceiverId(request.getReceiverId());
        transfer.setAmount(request.getAmount());
        transfer.setCreatedAt(LocalDateTime.now());

        return transferRepository.save(transfer);
    }
}