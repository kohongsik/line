package com.naver.line.demo.account.app;

import com.naver.line.demo.account.dto.AccountDto;
import com.naver.line.demo.account.mapper.AccountMapper;
import com.naver.line.demo.common.exceptions.NotValidException;
import javassist.tools.web.BadHttpRequest;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.MethodArgumentNotValidException;

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
                // 사용자가 금일 생산한 계좌가 있는경우.
                List<AccountDto> todayDataList = accountMapper.findByUserIdToday(param);
                if (todayDataList != null && todayDataList.size() > 0) hasErr = true;
                // 사용자가 이미 3개의 활성화된 계좌가 있는 경우 .
                int cnt = accountMapper.findAccountCntByUserId(param);
                if (cnt >= 3) hasErr = true;
                break;
        }
        if (hasErr) {
            throw new NotValidException("data is invalid.");
        }
    }
    private void checkValid (AccountDto param) {
        if (
                StringUtils.isEmpty(param.getId()) ||
                StringUtils.isEmpty(param.getUserId()) ||
                StringUtils.isEmpty(param.getNumber()) ||
                StringUtils.isEmpty(param.getAmount()) ||
                StringUtils.isEmpty(param.getStatus()) ||
                StringUtils.isEmpty(param.getTransferLimit()) ||
                StringUtils.isEmpty(param.getDailyTransferLimit())
        ) {
            throw new NotValidException("data is invalid.");
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
        return accountMapper.findByNumber(accountDto);
    }
}
