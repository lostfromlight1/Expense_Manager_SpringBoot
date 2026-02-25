package com.talent.expensemanager.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BaseResponse<T> {
    private Integer httpStatusCode;
    private String apiName;
    private String apiId;
    private String message;

    @Builder.Default
    private LocalDateTime systemDateTime = LocalDateTime.now();

    private T data;
}