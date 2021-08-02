package com.n26.service;

import com.n26.cache.TransactionsCache;
import com.n26.dto.StatisticsResponse;
import com.n26.dto.TransactionRequest;
import com.n26.exception.OldTransactionException;
import com.n26.exception.UnprocessableEntityException;
import com.n26.model.Transaction;
import com.n26.util.BigDecimalUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
@SpringBootTest
public class TransactionServiceTest {

    private final String VALID_AMOUNT = "12.513";

    @InjectMocks
    TransactionService transactionService;

    @Spy
    TransactionsCache cache;

    @Test
    public void saveValidTransaction() {
        //Arrange
        Instant instant = Instant.now();
        TransactionRequest transactionRequest = buildTransactionRequestWithValidAmount(instant);

        Transaction expectedTransaction = new Transaction();
        expectedTransaction.setAmount(new BigDecimal(VALID_AMOUNT));
        expectedTransaction.setTimestamp(instant);

        //Act
        transactionService.save(transactionRequest);

        //Assert
        Optional<Transaction> optTransaction = cache.getTransactions().values().stream().findFirst();
        assertTrue(optTransaction.isPresent());
        assertThat(optTransaction.get().getAmount(), is(expectedTransaction.getAmount()));
        assertThat(optTransaction.get().getTimestamp(), is(expectedTransaction.getTimestamp()));
    }

    @Test(expected = OldTransactionException.class)
    public void saveTransactionWithOldTimestamp() {
        TransactionRequest transactionRequest = new TransactionRequest();
        transactionRequest.setAmount(VALID_AMOUNT);
        transactionRequest.setTimestamp("2018-07-17T09:59:51.312Z");
        transactionService.save(transactionRequest);
    }

    @Test(expected = UnprocessableEntityException.class)
    public void saveTransactionWithFutureTimestamp() {
        TransactionRequest transactionRequest = new TransactionRequest();
        transactionRequest.setAmount(VALID_AMOUNT);
        transactionRequest.setTimestamp("3000-07-17T09:59:51.312Z");
        transactionService.save(transactionRequest);
    }

    @Test(expected = UnprocessableEntityException.class)
    public void saveTransactionWithNotParsableTimestamp() {
        TransactionRequest transactionRequest = new TransactionRequest();
        transactionRequest.setAmount(VALID_AMOUNT);
        transactionRequest.setTimestamp("2018-07-17T09:59:51.312");
        transactionService.save(transactionRequest);
    }

    @Test(expected = UnprocessableEntityException.class)
    public void saveTransactionWithNotParsableAmount() {
        TransactionRequest transactionRequest = new TransactionRequest();
        transactionRequest.setAmount("invalid-amount");
        transactionRequest.setTimestamp(Instant.now().toString());
        transactionService.save(transactionRequest);
    }

    @Test
    public void generateStatistics() {
        //Arrange
        int numTransactions = 5;
        String expectedMax = BigDecimalUtil.setCustomScale(new BigDecimal(5)).toString();
        String expectedMin = BigDecimalUtil.setCustomScale(new BigDecimal(1)).toString();
        double sum = 5+4+3+2+1;
        String expectedSum = BigDecimalUtil.setCustomScale(new BigDecimal(sum)).toString();
        String expectedAvg = BigDecimalUtil.setCustomScale(new BigDecimal(sum/numTransactions)).toString();
        when(cache.getTransactions()).thenReturn(buildTransactionsHashMap(numTransactions));

        //Act
        StatisticsResponse statistics = transactionService.generateStatistics();

        //Assert
        assertNotNull(statistics);
        assertEquals(numTransactions, statistics.getCount());
        assertEquals(expectedMax, statistics.getMax());
        assertEquals(expectedMin, statistics.getMin());
        assertEquals(expectedSum, statistics.getSum());
        assertEquals(expectedAvg, statistics.getAvg());
    }

    @Test
    public void deleteAllTransactions() {
        when(cache.getTransactions()).thenReturn(buildTransactionsHashMap(10));
        transactionService.deleteAllTransactions();
        assertTrue(cache.getTransactions().isEmpty());
    }

    private ConcurrentMap<UUID, Transaction> buildTransactionsHashMap(int numTransactions) {
        ConcurrentHashMap<UUID, Transaction> transactions = new ConcurrentHashMap<>();
        for (int i = 1; i <= numTransactions; i++) {
            Transaction transaction = new Transaction();
            transaction.setAmount(new BigDecimal(i));
            transaction.setTimestamp(Instant.now());
            transactions.put(UUID.randomUUID(), transaction);
        }
        return transactions;
    }

    private TransactionRequest buildTransactionRequestWithValidAmount(Instant instant) {
        TransactionRequest transactionRequest = new TransactionRequest();
        transactionRequest.setAmount(VALID_AMOUNT);
        transactionRequest.setTimestamp(instant.toString());
        return transactionRequest;
    }
}