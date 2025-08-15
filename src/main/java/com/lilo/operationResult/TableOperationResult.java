package com.lilo.operationResult;

import lombok.Data;

@Data
public class TableOperationResult {
    private String errorMessage;
    private boolean isSuccess;

    private TableOperationResult(boolean isSuccess, String errorMessage){
        this.isSuccess = isSuccess;
        this.errorMessage = errorMessage;
    }
    public static TableOperationResult fromFailure(String errorMessage){
        return new TableOperationResult(false, errorMessage);
    }
    public static TableOperationResult fromSuccess(){
        return new TableOperationResult(true,null);
    }
}
