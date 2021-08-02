package com.n26.cache;

import com.n26.model.Transaction;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Component
public class TransactionsCache {

    private final ConcurrentMap<UUID, Transaction> transactions = new ConcurrentHashMap<>();

    public ConcurrentMap<UUID, Transaction> getTransactions() {
        return transactions;
    }
}
