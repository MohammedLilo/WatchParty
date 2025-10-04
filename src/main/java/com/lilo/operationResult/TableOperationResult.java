package com.lilo.operationResult;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
public class TableOperationResult {
    private String errorMessage;
    private boolean isSuccess;
    private int suggestedStatusCode;
    private TableOperationResult(boolean isSuccess, String errorMessage){
        this.isSuccess = isSuccess;
        this.errorMessage = errorMessage;
    }
    private TableOperationResult(String errorMessage, int suggestedStatusCode){
        this.isSuccess = false;
        this.errorMessage = errorMessage;
        this.suggestedStatusCode = suggestedStatusCode;
    }
    public static TableOperationResult fromFailure(String errorMessage){
        return new TableOperationResult(false, errorMessage);
    }

    public static TableOperationResult fromFailure(String errorMessage,  int  suggestedStatusCode){
        return new TableOperationResult(errorMessage,  suggestedStatusCode);
    }
    public static TableOperationResult fromSuccess(){
        return new TableOperationResult(true,null);
    }
}
