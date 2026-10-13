
package com.payflow.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.payflow.entity.User;
import com.payflow.entity.Wallet;
import com.payflow.exception.EmailAlreadyExistsException;
import com.payflow.repository.UserRepository;
import com.payflow.repository.WalletRepository;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private WalletRepository walletRepository;

    @InjectMocks
    private UserService userService;

    @Test
    void shouldCreateUserAndWallet() {

        User user = new User();
        user.setName("Matheus");
        user.setEmail("matheus@teste.com");

        given(userRepository.existsByEmail(user.getEmail()))
                .willReturn(false);

        given(userRepository.save(user))
                .willReturn(user);

        User result = userService.save(user);

        assertEquals(user, result);

        verify(userRepository).save(user);
        verify(walletRepository).save(any(Wallet.class));
    }

    @Test
    void shouldRejectDuplicatedEmail() {

        User user = new User();
        user.setName("Outro Matheus");
        user.setEmail("matheus@teste.com");

        given(userRepository.existsByEmail(user.getEmail()))
                .willReturn(true);

        EmailAlreadyExistsException exception = assertThrows(
                EmailAlreadyExistsException.class,
                () -> userService.save(user)
        );

        assertEquals("E-mail já cadastrado", exception.getMessage());

        verify(userRepository, never()).save(any(User.class));
        verify(walletRepository, never()).save(any(Wallet.class));
    }
}
