package com.payflow.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.payflow.dto.TransferRequest;
import com.payflow.entity.Transfer;
import com.payflow.entity.Wallet;
import com.payflow.repository.TransferIdempotencyKeyRepository;
import com.payflow.repository.TransferRepository;
import com.payflow.repository.WalletRepository;

@Service
public class TransferService {

    private final TransferRepository transferRepository;
    private final WalletRepository walletRepository;
    private final TransferIdempotencyKeyRepository idempotencyKeyRepository;

    public TransferService(
            TransferRepository transferRepository,
            WalletRepository walletRepository,
            TransferIdempotencyKeyRepository idempotencyKeyRepository) {
        this.transferRepository = transferRepository;
        this.walletRepository = walletRepository;
        this.idempotencyKeyRepository = idempotencyKeyRepository;
    }

    @Transactional
    public Transfer transfer(
            TransferRequest request,
            String idempotencyKey) {

        if (request == null) {
            throw new IllegalArgumentException(
                    "Os dados da transferência são obrigatórios");
        }

        if (idempotencyKey == null
                || idempotencyKey.isBlank()
                || idempotencyKey.length() > 100) {
            throw new IllegalArgumentException(
                    "A chave de idempotência é obrigatória "
                    + "e deve ter até 100 caracteres");
        }

        Long senderId = request.getSenderId();
        Long receiverId = request.getReceiverId();
        BigDecimal amount = request.getAmount();

        if (senderId == null || receiverId == null) {
            throw new IllegalArgumentException(
                    "Os IDs do remetente e do destinatário são obrigatórios");
        }

        if (amount == null
                || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "O valor da transferência deve ser maior que zero");
        }

        if (senderId.equals(receiverId)) {
            throw new IllegalArgumentException(
                    "Origem e destino devem ser diferentes");
        }

        // Reserva ou bloqueia a chave dentro da transação atual.
        idempotencyKeyRepository.lockOrCreate(idempotencyKey);

        // Verifica se esta solicitação já foi processada.
        var existingTransfer = transferRepository
                .findByIdempotencyKey(idempotencyKey);

        if (existingTransfer.isPresent()) {
            Transfer existing = existingTransfer.get();

            boolean sameRequest =
                    existing.getSenderId().equals(senderId)
                    && existing.getReceiverId().equals(receiverId)
                    && existing.getAmount().compareTo(amount) == 0;

            if (!sameRequest) {
                throw new IllegalArgumentException(
                        "A chave de idempotência já foi utilizada "
                        + "com dados diferentes");
            }

            return existing;
        }

        Wallet senderWallet;
        Wallet receiverWallet;

        // Bloqueia as carteiras sempre na mesma ordem.
        if (senderId < receiverId) {
            senderWallet = walletRepository
                    .findByUserIdForUpdate(senderId)
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Carteira do remetente não encontrada"));

            receiverWallet = walletRepository
                    .findByUserIdForUpdate(receiverId)
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Carteira do destinatário não encontrada"));
        } else {
            receiverWallet = walletRepository
                    .findByUserIdForUpdate(receiverId)
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Carteira do destinatário não encontrada"));

            senderWallet = walletRepository
                    .findByUserIdForUpdate(senderId)
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Carteira do remetente não encontrada"));
        }

        if (senderWallet.getBalance().compareTo(amount) < 0) {
            throw new IllegalArgumentException("Saldo insuficiente");
        }

        senderWallet.setBalance(
                senderWallet.getBalance().subtract(amount));

        receiverWallet.setBalance(
                receiverWallet.getBalance().add(amount));

        walletRepository.save(senderWallet);
        walletRepository.save(receiverWallet);

        Transfer transfer = new Transfer();
        transfer.setSenderId(senderId);
        transfer.setReceiverId(receiverId);
        transfer.setAmount(amount);
        transfer.setIdempotencyKey(idempotencyKey);
        transfer.setCreatedAt(LocalDateTime.now());

        return transferRepository.save(transfer);
    }
}