package com.naver.line.demo.account;

import com.naver.line.demo.account.app.AccountService;
import com.naver.line.demo.account.dto.AccountDto;
import com.naver.line.demo.utils.ApiUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.naver.line.demo.utils.ApiUtils.ApiResult;

import static com.naver.line.demo.utils.ApiUtils.success;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/accounts")
public class AccountController {
    final AccountService accountService;
  /**
   * 1. 계좌 개설
   */
  @PostMapping()
  public ApiResult<AccountDto> account (@RequestHeader("X-USER-ID") Integer userId,@RequestBody AccountDto accountDto) {
      AccountDto ret = accountService.createAccount(userId, accountDto);
      return success(ret);
  }
  /**
   * 2. 계좌 비활성화
   */

  /**
   * 3. 계좌 이체 한도 수정
   */

  /**
   * 5. 계좌 입출금 내역
   */
}
