package com.n26.service;

import com.n26.cache.TransactionsCache;
import com.n26.dto.StatisticsResponse;
import com.n26.dto.TransactionRequest;
import com.n26.exception.OldTransactionException;
import com.n26.exception.UnprocessableEntityException;
import com.n26.model.Transaction;
import com.n26.util.BigDecimalUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.validator.routines.BigDecimalValidator;
import org.eclipse.collections.impl.collector.BigDecimalSummaryStatistics;
import org.eclipse.collections.impl.collector.Collectors2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class TransactionService {

    @Autowired
    TransactionsCache cache;

    private static final int TIME_IN_CACHE = 60000;

    public void save(TransactionRequest transactionRequest) {
        if (!BigDecimalValidator.getInstance().isValid(transactionRequest.getAmount())) {
            log.warn("Transaction amount is not parsable");
            throw new UnprocessableEntityException();
        }
        Transaction transaction = new Transaction();
        transaction.setAmount(new BigDecimal(transactionRequest.getAmount()));
        try {
            transaction.setTimestamp(Instant.parse(transactionRequest.getTimestamp()));
        } catch (DateTimeParseException e) {
            log.warn("Transaction timestamp is not parsable");
            throw new UnprocessableEntityException();
        }
        long now = Instant.now().toEpochMilli();
        long delay = now - transaction.getTimestamp().toEpochMilli();
        if (delay >= TIME_IN_CACHE) {
            log.warn("Transaction timestamp is older than 60 seconds");
            throw new OldTransactionException();
        } else if ( delay < 0) {
            log.warn("Transaction timestamp is in the future");
            throw new UnprocessableEntityException();
        }
        UUID id = UUID.randomUUID();
        cache.getTransactions().put(id, transaction);

        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.schedule(() ->
                cache.getTransactions().remove(id), TIME_IN_CACHE - delay, TimeUnit.MILLISECONDS);
        scheduler.shutdown();
    }

    public StatisticsResponse generateStatistics() {
        BigDecimalSummaryStatistics stats = cache.getTransactions().values().stream()
                .map(Transaction::getAmount)
                .collect(Collectors2.summarizingBigDecimal(each -> each));

        StatisticsResponse statistics = new StatisticsResponse();
        statistics.setCount(stats.getCount());
        statistics.setSum(BigDecimalUtil.setCustomScale(stats.getSum()).toString());
        statistics.setMax(BigDecimalUtil.setCustomScale(stats.getMaxOptional().orElse(BigDecimal.ZERO)).toString());
        statistics.setMin(BigDecimalUtil.setCustomScale(stats.getMinOptional().orElse(BigDecimal.ZERO)).toString());
        statistics.setAvg(BigDecimalUtil.setCustomScale(stats.getAverage()).toString());
        return statistics;
    }

    public void deleteAllTransactions() {
        cache.getTransactions().clear();
    }
}
