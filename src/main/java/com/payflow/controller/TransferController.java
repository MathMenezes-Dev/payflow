package com.payflow.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.payflow.dto.TransferRequest;
import com.payflow.entity.Transfer;
import com.payflow.service.TransferService;

@RestController
@RequestMapping("/transfers")
public class TransferController {

    private final TransferService transferService;

    public TransferController(TransferService transferService) {
        this.transferService = transferService;
    }

    @PostMapping
    public ResponseEntity<Transfer> transfer(@RequestBody TransferRequest request) {
        Transfer transfer = transferService.transfer(request);

        return ResponseEntity.ok(transfer);
    }
}