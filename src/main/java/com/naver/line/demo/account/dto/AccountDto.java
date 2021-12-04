package com.naver.line.demo.account.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AccountDto {
    private String id; // not null
    private String userId; // not null
    private String number; // not null
    private String amount; // not null
    private String status; // not null
    private String transferLimit; // not null
    private String dailyTransferLimit; // not null
    private String createdAt; // not null , default now
    private String updatedAt;
}
