package com.naver.line.demo.transfer;

import com.naver.line.demo.account.dto.AccountDto;
import com.naver.line.demo.transfer.dto.TransferDto;
import com.naver.line.demo.utils.ApiUtils;
import org.springframework.web.bind.annotation.*;

import static com.naver.line.demo.utils.ApiUtils.success;

@RestController
@RequestMapping("/api/transfers")
public class TransferController {
  /**
   * 4. 이체
   */
  @PostMapping(value = "/transfers/withdraw")
  public ApiUtils.ApiResult<TransferDto> transaction (@RequestHeader("X-USER-ID") Integer userId, @RequestBody TransferDto transferDto) {

      return success(null);
  }
}
