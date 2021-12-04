package com.naver.line.demo.account.mapper;

import com.naver.line.demo.account.dto.AccountDto;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface AccountMapper {
    List<AccountDto> findByUserIdToday(AccountDto accountDto);
    AccountDto findByNumberOrId(AccountDto accountDto);
    int findAccountCntByUserId(AccountDto accountDto);
    String getRandomAccountNo(AccountDto accountDto);
    int checkNumberDupl(AccountDto accountDto);
    int createAccount(AccountDto accountDto);
    int updateState(AccountDto accountDto);
    int updateTransferLimit(AccountDto accountDto);
}
