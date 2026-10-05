package com.payflow.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.payflow.dto.WalletResponse;
import com.payflow.service.WalletService;

@RestController
@RequestMapping("/users")
public class WalletController {

private final WalletService walletService;

public WalletController(WalletService walletService) {
    this.walletService = walletService;
}

@GetMapping("/{userId}/wallet")
public WalletResponse findWallet(@PathVariable Long userId) {
    return walletService.findByUserId(userId);
}

}