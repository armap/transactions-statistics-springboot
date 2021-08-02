package com.n26.dto;

import lombok.Data;

@Data
public class TransactionRequest {
    private String amount;
    private String timestamp;
}
