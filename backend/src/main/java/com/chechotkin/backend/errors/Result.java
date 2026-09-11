package com.chechotkin.backend.errors;

import java.util.function.Function;

public class Result<T> {
    private final T result;
    private final BaseErrors error;
    public Result(T result) {
        this.result = result;
        this.error =null;
    }
    public Result(BaseErrors error){
        this.error = error;
        this.result = null;
    }

    public boolean isSuccess(){
        return this.error == null;
    }

    public T getResult(){
        return  result;
    }

    public BaseErrors getError(){
        return error;
    }

    public static <T> Result<T> success(T result){
        return new Result<>(result);
    }

    public static <T> Result<T> error(BaseErrors error){
        return new Result<>(error);
    }

    public<R> R mapError(Function<BaseErrors, R> mapper){
        return mapper.apply(error);
    }
    public<R> R mapResult(Function<T, R> mapper){
        return mapper.apply(result);
    }
}
