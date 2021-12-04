package com.naver.line.demo.account.app;

import com.naver.line.demo.account.dto.AccountDto;

public interface AccountService {
    AccountDto createAccount(int userId, AccountDto accountDto);
    AccountDto disabledAccount(int userId, int id);
}
