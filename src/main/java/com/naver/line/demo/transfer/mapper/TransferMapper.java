package com.naver.line.demo.transfer.mapper;

import com.naver.line.demo.transfer.dto.TransferDto;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TransferMapper {
    String findTodayTotalAmount(TransferDto transferDto);
    int createBalanceTransaction(TransferDto transferDto);
    int createTransfer(TransferDto transferDto);
}
