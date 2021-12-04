package com.naver.line.demo.transfer.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TransferDto {
    private String senderAccountNumber; // string / 출금 계좌번호	000-00-00000 형식
    private String receiverAccountNumber; // string / 입금 계좌번호	000-00-00000 형식
    private String amount; // 금액 / 최소 10 이상
    private String senderNote; // 내통장표시 / 최소 2글자 ~ 최대 10글자
    private String receiverNote; // 받는 통장 표시 / 최소 2글자 ~ 최대 10글자
    private String id;
    private String userId;
    private String withdrawId;
    private String depositId;
    private String createdAt;

    // balance table 정보
    private String accountId;
    private String type;
    private String beforeBalanceAmount;
    private String note;

    private String updatedAt;

}
