package dev.kuku.authsome.model;

import lombok.AllArgsConstructor;
import lombok.ToString;

@SuppressWarnings("unused")
@AllArgsConstructor
@ToString
public class ResponseModel<T> {
    public String message;
    public T data;

    public static <T> ResponseModel<T> of(T data) {
        return new ResponseModel<>(null, data);
    }

    public static <T> ResponseModel<T> of(T data, String message) {
        return new ResponseModel<>(message, data);
    }
}
