package com.naver.line.demo.transfer.app;

import com.naver.line.demo.transfer.dto.TransferDto;

public interface TransferService {
    TransferDto transaction(int userId, TransferDto transferDto);
}
