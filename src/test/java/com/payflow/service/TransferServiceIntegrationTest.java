package com.payflow.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.payflow.dto.TransferRequest;
import com.payflow.entity.Transfer;
import com.payflow.entity.User;
import com.payflow.entity.Wallet;
import com.payflow.repository.TransferRepository;
import com.payflow.repository.UserRepository;
import com.payflow.repository.WalletRepository;
import com.payflow.service.TransferService;

@SpringBootTest
@Transactional
class TransferServiceIntegrationTest {

    @Autowired
    private TransferService transferService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private TransferRepository transferRepository;

    @Test
    void shouldPersistTransferAndUpdateWalletBalances() {
        User sender = new User();
        sender.setName("Remetente Teste");
        sender.setEmail("remetente.integracao@payflow.test");
        sender = userRepository.save(sender);

        User receiver = new User();
        receiver.setName("Destinatario Teste");
        receiver.setEmail("destinatario.integracao@payflow.test");
        receiver = userRepository.save(receiver);

        Wallet senderWallet = new Wallet(sender);
        senderWallet.setBalance(new BigDecimal("100.00"));
        walletRepository.save(senderWallet);

        Wallet receiverWallet = new Wallet(receiver);
        receiverWallet.setBalance(new BigDecimal("20.00"));
        walletRepository.save(receiverWallet);

        TransferRequest request = new TransferRequest();
        request.setSenderId(sender.getId());
        request.setReceiverId(receiver.getId());
        request.setAmount(new BigDecimal("30.00"));

        Transfer transfer = transferService.transfer(request);

        walletRepository.flush();
        transferRepository.flush();

        Wallet updatedSenderWallet = walletRepository
                .findByUserId(sender.getId())
                .orElseThrow();

        Wallet updatedReceiverWallet = walletRepository
                .findByUserId(receiver.getId())
                .orElseThrow();

        assertEquals(
                0,
                new BigDecimal("70.00").compareTo(
                        updatedSenderWallet.getBalance()));

        assertEquals(
                0,
                new BigDecimal("50.00").compareTo(
                        updatedReceiverWallet.getBalance()));

        assertNotNull(transfer.getId());
        assertNotNull(transfer.getCreatedAt());

        Transfer savedTransfer = transferRepository.findById(
                transfer.getId()).orElseThrow();

        assertEquals(sender.getId(), savedTransfer.getSenderId());
        assertEquals(receiver.getId(), savedTransfer.getReceiverId());

        assertEquals(
                0,
                new BigDecimal("30.00").compareTo(savedTransfer.getAmount()));
    }
}