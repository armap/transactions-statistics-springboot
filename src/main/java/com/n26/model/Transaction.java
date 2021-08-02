package com.n26.model;

import com.n26.util.BigDecimalUtil;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@NoArgsConstructor
public class Transaction {

    private BigDecimal amount;
    private Instant timestamp;

    public void setAmount(BigDecimal amount) {
        this.amount = BigDecimalUtil.setCustomScale(amount);
    }
}
