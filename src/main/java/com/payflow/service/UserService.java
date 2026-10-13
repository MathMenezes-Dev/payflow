package com.payflow.service;

import org.springframework.stereotype.Service;

import com.payflow.entity.User;
import com.payflow.entity.Wallet;
import com.payflow.repository.UserRepository;
import com.payflow.repository.WalletRepository;
import org.springframework.transaction.annotation.Transactional;

import com.payflow.exception.EmailAlreadyExistsException;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final WalletRepository walletRepository;

    public UserService(UserRepository userRepository, WalletRepository walletRepository) {
        this.userRepository = userRepository;
        this.walletRepository = walletRepository;
    }

    
    @Transactional
    public User save(User user) {

    	if (userRepository.existsByEmail(user.getEmail())) {
    	    throw new EmailAlreadyExistsException("E-mail já cadastrado");
    	}
    	
        User savedUser = userRepository.save(user);

        Wallet wallet = new Wallet(savedUser);

        walletRepository.save(wallet);

        return savedUser;
    }
}