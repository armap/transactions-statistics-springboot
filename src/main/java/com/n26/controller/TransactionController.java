package com.n26.controller;

import com.n26.dto.StatisticsResponse;
import com.n26.dto.TransactionRequest;
import com.n26.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class TransactionController {

    @Autowired
    TransactionService transactionService;

    @PostMapping("/transactions")
    @ResponseStatus(HttpStatus.CREATED)
    void newTransaction(@RequestBody TransactionRequest transactionRequest) {
        transactionService.save(transactionRequest);
    }

    @DeleteMapping("/transactions")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void deleteAllTransactions() {
        transactionService.deleteAllTransactions();
    }

    @GetMapping("/statistics")
    ResponseEntity<StatisticsResponse> getStatistics() {
        return new ResponseEntity<>(transactionService.generateStatistics(), HttpStatus.OK);
    }
}
