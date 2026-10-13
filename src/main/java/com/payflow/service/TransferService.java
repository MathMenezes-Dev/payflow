
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

        if (request == null) {
            throw new IllegalArgumentException(
                    "Os dados da transferência são obrigatórios");
        }

        Long senderId = request.getSenderId();
        Long receiverId = request.getReceiverId();
        BigDecimal amount = request.getAmount();

        if (senderId == null || receiverId == null) {
            throw new IllegalArgumentException(
                    "Os IDs do remetente e do destinatário são obrigatórios");
        }

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "O valor da transferência deve ser maior que zero");
        }

        if (senderId.equals(receiverId)) {
            throw new IllegalArgumentException(
                    "Origem e destino devem ser diferentes");
        }

        Wallet senderWallet;
        Wallet receiverWallet;

        if (senderId < receiverId) {

            senderWallet = walletRepository.findByUserIdForUpdate(senderId)
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Carteira do remetente não encontrada"));

            receiverWallet = walletRepository.findByUserIdForUpdate(receiverId)
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Carteira do destinatário não encontrada"));

        } else {

            receiverWallet = walletRepository.findByUserIdForUpdate(receiverId)
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Carteira do destinatário não encontrada"));

            senderWallet = walletRepository.findByUserIdForUpdate(senderId)
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Carteira do remetente não encontrada"));
        }

        if (senderWallet.getBalance().compareTo(amount) < 0) {
            throw new IllegalArgumentException("Saldo insuficiente");
        }

        senderWallet.setBalance(senderWallet.getBalance().subtract(amount));
        receiverWallet.setBalance(receiverWallet.getBalance().add(amount));

        walletRepository.save(senderWallet);
        walletRepository.save(receiverWallet);

        Transfer transfer = new Transfer();
        transfer.setSenderId(senderId);
        transfer.setReceiverId(receiverId);
        transfer.setAmount(amount);
        transfer.setCreatedAt(LocalDateTime.now());

        return transferRepository.save(transfer);
    }
}
