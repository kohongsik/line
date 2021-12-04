package com.naver.line.demo.transfer;

import com.naver.line.demo.account.dto.AccountDto;
import com.naver.line.demo.transfer.app.TransferService;
import com.naver.line.demo.transfer.dto.TransferDto;
import com.naver.line.demo.utils.ApiUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import static com.naver.line.demo.utils.ApiUtils.success;

@RestController
@RequestMapping("/api/transfers")
@RequiredArgsConstructor
public class TransferController {
  /**
   * 4. 이체
   */
  final TransferService transferService;
  @PostMapping(value = "/transfers/withdraw")
  public ApiUtils.ApiResult<TransferDto> transaction (@RequestHeader("X-USER-ID") Integer userId, @RequestBody TransferDto transferDto) {
        TransferDto ret = transferService.transaction(userId, transferDto);
      return success(ret);
  }
}
