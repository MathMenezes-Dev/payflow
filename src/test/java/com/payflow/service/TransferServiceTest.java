package com.payflow.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.payflow.dto.TransferRequest;
import com.payflow.entity.Transfer;
import com.payflow.entity.Wallet;
import com.payflow.repository.TransferRepository;
import com.payflow.repository.WalletRepository;

@ExtendWith(MockitoExtension.class)
class TransferServiceTest {

    @Mock
    private TransferRepository transferRepository;

    @Mock
    private WalletRepository walletRepository;

    @InjectMocks
    private TransferService transferService;

    private Wallet senderWallet;
    private Wallet receiverWallet;

    @BeforeEach
    void setUp() {
        senderWallet = new Wallet();
        senderWallet.setBalance(new BigDecimal("100.00"));

        receiverWallet = new Wallet();
        receiverWallet.setBalance(new BigDecimal("20.00"));
    }

    @Test
    void shouldTransferSuccessfully() {
        TransferRequest request = createRequest(
                1L, 2L, new BigDecimal("30.00"));

        when(walletRepository.findByUserIdForUpdate(1L))
                .thenReturn(Optional.of(senderWallet));
        when(walletRepository.findByUserIdForUpdate(2L))
                .thenReturn(Optional.of(receiverWallet));
        when(transferRepository.save(any(Transfer.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Transfer result = transferService.transfer(request);

        assertEquals(new BigDecimal("70.00"), senderWallet.getBalance());
        assertEquals(new BigDecimal("50.00"), receiverWallet.getBalance());
        assertEquals(1L, result.getSenderId());
        assertEquals(2L, result.getReceiverId());
        assertEquals(new BigDecimal("30.00"), result.getAmount());
        assertNotNull(result.getCreatedAt());

        verify(walletRepository).save(senderWallet);
        verify(walletRepository).save(receiverWallet);
        verify(transferRepository).save(any(Transfer.class));
    }

    @Test
    void shouldRejectNullRequest() {
        assertThrows(
                IllegalArgumentException.class,
                () -> transferService.transfer(null));
    }

    @Test
    void shouldRejectMissingUserIds() {
        TransferRequest request = createRequest(
                null, 2L, new BigDecimal("10.00"));

        assertThrows(
                IllegalArgumentException.class,
                () -> transferService.transfer(request));
    }

    @Test
    void shouldRejectZeroOrNegativeAmount() {
        TransferRequest zeroRequest = createRequest(
                1L, 2L, BigDecimal.ZERO);

        TransferRequest negativeRequest = createRequest(
                1L, 2L, new BigDecimal("-10.00"));

        assertThrows(
                IllegalArgumentException.class,
                () -> transferService.transfer(zeroRequest));

        assertThrows(
                IllegalArgumentException.class,
                () -> transferService.transfer(negativeRequest));
    }

    @Test
    void shouldRejectTransferToSameUser() {
        TransferRequest request = createRequest(
                1L, 1L, new BigDecimal("10.00"));

        assertThrows(
                IllegalArgumentException.class,
                () -> transferService.transfer(request));
    }

    @Test
    void shouldRejectInsufficientBalance() {
        TransferRequest request = createRequest(
                1L, 2L, new BigDecimal("150.00"));

        when(walletRepository.findByUserIdForUpdate(1L))
                .thenReturn(Optional.of(senderWallet));
        when(walletRepository.findByUserIdForUpdate(2L))
                .thenReturn(Optional.of(receiverWallet));

        assertThrows(
                IllegalArgumentException.class,
                () -> transferService.transfer(request));

        assertEquals(new BigDecimal("100.00"), senderWallet.getBalance());
        assertEquals(new BigDecimal("20.00"), receiverWallet.getBalance());
    }

    @Test
    void shouldRejectTransferWhenSenderWalletDoesNotExist() {
        TransferRequest request = createRequest(
                1L, 2L, new BigDecimal("10.00"));

        when(walletRepository.findByUserIdForUpdate(1L))
                .thenReturn(Optional.empty());

        assertThrows(
                IllegalArgumentException.class,
                () -> transferService.transfer(request));
    }

    @Test
    void shouldRejectTransferWhenReceiverWalletDoesNotExist() {
        TransferRequest request = createRequest(
                1L, 2L, new BigDecimal("10.00"));

        when(walletRepository.findByUserIdForUpdate(1L))
                .thenReturn(Optional.of(senderWallet));
        when(walletRepository.findByUserIdForUpdate(2L))
                .thenReturn(Optional.empty());

        assertThrows(
                IllegalArgumentException.class,
                () -> transferService.transfer(request));
    }

    private TransferRequest createRequest(
            Long senderId,
            Long receiverId,
            BigDecimal amount) {

        TransferRequest request = new TransferRequest();
        request.setSenderId(senderId);
        request.setReceiverId(receiverId);
        request.setAmount(amount);

        return request;
    }
}