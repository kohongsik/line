package com.naver.line.demo.account.app;

import com.naver.line.demo.account.dto.AccountDto;

public interface AccountService {
    int createAccount(int userId, AccountDto accountDto);
}
