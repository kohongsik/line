package com.naver.line.demo.account.app;

import com.naver.line.demo.account.dto.AccountDto;
import com.naver.line.demo.account.mapper.AccountMapper;
import com.naver.line.demo.common.exceptions.ForbiddenException;
import com.naver.line.demo.common.exceptions.NotFoundException;
import com.naver.line.demo.common.exceptions.NotValidException;
import javassist.tools.web.BadHttpRequest;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService{
    final AccountMapper accountMapper;
    private void isValidate(String method, int userId, AccountDto param){
        boolean hasErr = false;
        switch(method) {
            case "create" :
                // 데이터 검증 로직.
                checkValid(param);
                checkLimit(param);
                // 사용자가 금일 생산한 계좌가 있는경우.
                List<AccountDto> todayDataList = accountMapper.findByUserIdToday(param);
                if (todayDataList != null && todayDataList.size() > 0) hasErr = true;
                // 사용자가 이미 3개의 활성화된 계좌가 있는 경우 .
                int cnt = accountMapper.findAccountCntByUserId(param);
                if (cnt >= 3) hasErr = true;
                break;
            case "disabled" :
                // 계좌를 찾을 수 없을 경우 404 Not Found으로 응답합니다.
                if (param == null) {
                    throw new NotFoundException("data Not found");
                }
                // 사용자가 계좌의 소유자가 아닌 경우 403 Forbidden으로 응답합니다.
                int dbUserId = Integer.parseInt(param.getUserId());
                if (dbUserId != userId) {
                    // 403
                    throw new ForbiddenException("forbidden exception");
                }
                // 사용자의 계좌의 상태가 이미 DISABLED인 경우 400 Bad Request로 응답합니다.
                if ("DISABLED".equals(param.getStatus())) {
                    hasErr = true;
                }
                // 사용자의 계좌의 잔액이 0원이 아닌 경우 400 Bad Request로 응답합니다.
                if (new BigDecimal(param.getAmount()).compareTo(BigDecimal.ZERO) > 0) {
                    hasErr = true;
                }
                break;
            case "updateTransferLimit" :
                if (param == null) {
                    throw new NotFoundException("data Not found");
                }
                // 데이터 검증에 실패했을 경우 400 Bad Request로 응답합니다.
                checkValid(param);
                // 사용자가 계좌의 소유자가 아닌 경우 403 Forbidden으로 응답합니다.
                if (userId != Integer.parseInt(param.getUserId())) {
                    throw new ForbiddenException("forbidden");
                }
                // 사용자의 계좌가 비활성화 상태인 경우 400 Bad Request로 응답합니다.
                if ("DISABLED".equals(param.getStatus())) {
                    hasErr = true;
                }
                break;
        }
        if (hasErr) {
            throw new NotValidException("data is invalid.");
        }
    }
    private void checkValid (AccountDto param) {
        if (
                StringUtils.isEmpty(param.getTransferLimit()) ||
                StringUtils.isEmpty(param.getDailyTransferLimit())
        ) {
            throw new NotValidException("data is invalid.");
        }
    }
    private void checkLimit (AccountDto param) {
        final int maxTransferLimit = 5000000;
        final int maxDailyTransferLimit = 10000000;
        boolean hasLimitErr = false;
        int transferLimmit = Integer.parseInt(param.getTransferLimit());
        int dailyTranferLimit = Integer.parseInt(param.getDailyTransferLimit());
        if (transferLimmit < 0 || transferLimmit > maxTransferLimit) hasLimitErr = true;
        if (dailyTranferLimit < 0 || dailyTranferLimit > maxDailyTransferLimit) hasLimitErr = true;
        if (hasLimitErr) {
            throw new NotValidException("limit error.");
        }
    }
    @Override
    public AccountDto createAccount(int userId, AccountDto accountDto) {
        int ret = 0;
        accountDto.setUserId(String.valueOf(userId));
        /*
            accounts 테이블에 데이터를 저장합니다.
            계좌 상태는 ENABLED로 저장합니다.
            계좌 번호는 중복되지 않는 형식으로 생성해주세요.
            형식: 000-00-00000 > 전략 : 난수.
            응답으로 생성된 계좌 정보를 내려줍니다.
        */
        isValidate("create", userId, accountDto);
        accountDto.setStatus("ENABLED");
        boolean createNumber = false;
        while(!createNumber) {
            String accountNo = accountMapper.getRandomAccountNo(AccountDto.builder().build());
            String number = accountNo.substring(0, 3) + "-" + accountNo.substring(3, 5) + "-" + accountNo.substring(5);
            int cnt = accountMapper.checkNumberDupl(AccountDto.builder().number(number).build());
            if (cnt == 0) {
                createNumber = true;
                accountDto.setNumber(number);
            }
        }
        ret += accountMapper.createAccount(accountDto);
        return accountMapper.findByNumberOrId(accountDto);
    }
    @Override
    public AccountDto disabledAccount(int userId, int id) {
        /*
            accounts 테이블에서 계좌 정보를 가져옵니다.
            사용자의 계좌의 상태값을 DISABLED로 저장합니다.
            응답으로 비활성화된 계좌 정보를 내려줍니다.
        */
        AccountDto dbData = accountMapper.findByNumberOrId(AccountDto.builder().id(String.valueOf(id)).build());
        isValidate("disabled", userId, dbData);
        dbData.setStatus("DISABLED");
        accountMapper.updateState(dbData);
        return accountMapper.findByNumberOrId(AccountDto.builder().id(String.valueOf(id)).build());
    }
    @Override
    public AccountDto updateTransferLimit(int userId, int id, AccountDto accountDto) {
        /*
            응답으로 수정된 계좌 정보를 내려줍니다.
        */
        AccountDto dbData = accountMapper.findByNumberOrId(AccountDto.builder().id(String.valueOf(id)).build());
        isValidate("updateTransferLimit", userId, dbData);
        checkLimit(accountDto);
        dbData.setTransferLimit(accountDto.getTransferLimit());
        dbData.setDailyTransferLimit(accountDto.getDailyTransferLimit());
        accountMapper.updateTransferLimit(dbData);
        return accountMapper.findByNumberOrId(AccountDto.builder().id(String.valueOf(id)).build());
    }
}
