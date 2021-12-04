package com.naver.line.demo.transfer.app;

import com.naver.line.demo.account.app.AccountService;
import com.naver.line.demo.account.dto.AccountDto;
import com.naver.line.demo.account.mapper.AccountMapper;
import com.naver.line.demo.common.exceptions.ForbiddenException;
import com.naver.line.demo.common.exceptions.NotValidException;
import com.naver.line.demo.transfer.dto.TransferDto;
import com.naver.line.demo.transfer.mapper.TransferMapper;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@Transactional
@RequiredArgsConstructor
public class TransferServiceImpl implements TransferService{
    final AccountService accountService;
    final AccountMapper accountMapper;
    final TransferMapper transferMapper;
    private boolean isAccountNoFormat (String accountNo) {
        if (accountNo.length() != 12) return false;
        final String dash = "-";
        int[] dashIdx = new int [] {3, 6};
        int leftIdx = 0;
        for (int i = 0; i <= 1; i++) {
            int pos = dashIdx[i];
            if (!dash.equals(accountNo.substring(pos, pos + 1))) {
                return false;
            }
        }
        String splitStr[] = accountNo.split(dash);
        for (int i = 0; i < splitStr.length; i++) {
            try {
                int _val = Integer.parseInt(splitStr[i]);
            } catch(Exception e) {
                // 숫자 포멧이 아님.
                return false;
            }
        }
        return true;
    }
    @Override
    public TransferDto transaction(int userId, TransferDto transferDto) {
        // 데이터 검증에 실패했을 경우 400 Bad Request로 응답합니다.
        /**
        senderAccountNumber; // string / 출금 계좌번호	000-00-00000 형식 / 필수
        receiverAccountNumber; // string / 입금 계좌번호	000-00-00000 형식 / 필수
        amount; // 금액 / 최소 10 이상 / 필수
        senderNote; // 내통장표시 / 최소 2글자 ~ 최대 10글자
        receiverNote; // 받는 통장 표시 / 최소 2글자 ~ 최대 10글자
        **/
        if (
                StringUtils.isEmpty(transferDto.getSenderAccountNumber()) ||
                StringUtils.isEmpty(transferDto.getReceiverAccountNumber()) ||
                StringUtils.isEmpty(transferDto.getAmount())
        ) {
            throw new NotValidException("data is invalid.");
        }
        if (!isAccountNoFormat(transferDto.getSenderAccountNumber()) || 
        !isAccountNoFormat(transferDto.getReceiverAccountNumber())) {
            // 계좌번호 format 오류
            throw new NotValidException("data is invalid.");
        }
        int sendorNoteSize = transferDto.getSenderNote().length();
        int receiverNoteSize = transferDto.getReceiverNote().length();
        if (sendorNoteSize < 2 || sendorNoteSize > 10) {
            throw new NotValidException("data is invalid.");
        }
        if (receiverNoteSize < 2 || receiverNoteSize > 10) {
            throw new NotValidException("data is invalid.");
        }
        // accounts 테이블에서 계좌 정보를 가져옵니다.
        AccountDto sender = accountMapper.findByNumberOrId(
                AccountDto
                        .builder()
                        .number(transferDto.getSenderAccountNumber())
                        .build()
        );
        AccountDto receiver = accountMapper.findByNumberOrId(
                AccountDto
                        .builder()
                        .number(transferDto.getReceiverAccountNumber())
                        .build()
        );
        transferDto.setUserId(String.valueOf(userId));
        // 출금 계좌나 입금 계좌를 찾을 수 없을 경우 404 Not Found으로 응답합니다.
        if (sender == null || receiver == null) {
            throw new NotValidException("not found account.");
        }
        // 사용자가 출금 계좌의 소유자가 아닌 경우 403 Forbidden으로 응답합니다.
        if (userId != Integer.parseInt(sender.getUserId())) {
            throw new ForbiddenException("forbidden");
        }
        // 출금 계좌나 입금 계좌가 비활성화 상태인 경우 400 Bad Request로 응답합니다.
        if ("DISABLED".equals(sender.getStatus()) || "DISABLED".equals(receiver.getStatus())) {
            throw new NotValidException("status is invalid.");
        }
        // 출금액이 출금 계좌의 잔액보다 많은 경우 400 Bad Request로 응답합니다.
        BigDecimal accountAmt = !StringUtils.isEmpty(sender.getAmount()) ? new BigDecimal(sender.getAmount()) : BigDecimal.ZERO;
        BigDecimal sendAmt = new BigDecimal(transferDto.getAmount());
        if (accountAmt.compareTo(sendAmt) < 0) {
            throw new NotValidException("amount balance err.");
        }
        // 이체 금액이 출금 계좌의 1회 이체 한도를 초과할 경우 400 Bad Request로 응답합니다.
        BigDecimal transferLimit = !StringUtils.isEmpty(sender.getTransferLimit()) ? new BigDecimal(sender.getTransferLimit()) : BigDecimal.ZERO;
        if (transferLimit.compareTo(sendAmt) < 0) {
            throw new NotValidException("amount is exceeded limit amount.");
        }
        // 당일 총 이체 금액과 이체 금액의 합이 출금 계좌의 1일 이체 한도를 초과할 경우 400 Bad Request로 응답합니다.
        // 당일 총 이체 금액은 00시 00분 00초 ~ 23시 59분 59초 사이 이체 금액의 총 합을 말합니다.
        BigDecimal dailyTransferLimit = !StringUtils.isEmpty(sender.getDailyTransferLimit()) ? new BigDecimal(sender.getDailyTransferLimit()) : BigDecimal.ZERO;
        String todayAmontStr = transferMapper.findTodayTotalAmount(transferDto);
        BigDecimal todayAmont = !StringUtils.isEmpty(todayAmontStr) ? new BigDecimal(todayAmontStr) : BigDecimal.ZERO;
        if (todayAmont.compareTo(todayAmont) >= 0) {
            throw new NotValidException("amount is exceeded limit amount.");
        }
        // 출금 정보를 balance_transactions 테이블에 저장합니다.
        // 타입은 WITHDRAW으로 저장합니다.
        // 출금 전 계좌의 잔액을 before_balance_amount에 저장합니다.
        // note에 sender_note를 저장합니다. sender_note 값이 없을 경우 입금 계좌의 고객명을 저장합니다.
        TransferDto withdrawInfo =
                TransferDto
                        .builder()
                        .userId(String.valueOf(userId))
                        .type("WITHDRAW")
                        .beforeBalanceAmount(sender.getAmount())
                        .note(transferDto.getSenderNote())
                        .accountId(sender.getId())
                        .build();
        transferMapper.createBalanceTransaction(withdrawInfo);
        // 출금 계좌의 잔액을 금액만큼 감소시킵니다.
        sender.setType("SUB");
        sender.setAmount(transferDto.getAmount());
        accountMapper.updateAmount(sender);
        // 입금 정보를 balance_transactions 테이블에 저장합니다.
        // 타입은 DEPOSIT으로 저장합니다.
        // 입금 전 계좌의 잔액을 before_balance_amount에 저장합니다.
        // note에 receiver_note 값을 저장합니다. receiver_note 값이 없을 경우 출금 계좌의 고객명을 저장합니다.
        // 입금 계좌의 잔액을 금액만큼 증가시킵니다.
        TransferDto depositInfo =
                TransferDto
                        .builder()
                        .userId(String.valueOf(userId))
                        .type("DEPOSIT")
                        .beforeBalanceAmount(receiver.getAmount())
                        .note(transferDto.getReceiverNote())
                        .accountId(receiver.getId())
                        .build();
        transferMapper.createBalanceTransaction(depositInfo);
        // 출금 계좌의 잔액을 금액만큼 감소시킵니다.
        receiver.setType("ADD");
        receiver.setAmount(transferDto.getAmount());
        accountMapper.updateAmount(receiver);
        // transfers 테이블에 데이터를 저장합니다.
        // 출금 정보 저장 후 생성된 ID를 withdraw_id에 저장합니다.
        // 입금 정보 저장 후 생성된 ID를 deposit_id에 저장합니다.
        // 응답으로 출금 거래 정보를 내려줍니다.
        transferMapper.createTransfer(transferDto);

        return null;
    }
}
