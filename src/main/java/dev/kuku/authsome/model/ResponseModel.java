package dev.kuku.authsome.model;

import lombok.AllArgsConstructor;
import lombok.ToString;

/**
 * Generic response model for API responses.
 * <p>
 * Encapsulates a message and a data payload of any type.
 *
 * @param <T> the type of the response data
 */
@SuppressWarnings("unused")
@AllArgsConstructor
@ToString
public class ResponseModel<T> {
    /**
     * The message describing the response or error (optional).
     */
    public String message;
    /**
     * The data payload of the response.
     */
    public T data;

    /**
     * Creates a response model with the given data and no message.
     *
     * @param data the response data
     * @param <T>  the type of the response data
     * @return a new ResponseModel instance
     */
    public static <T> ResponseModel<T> of(T data) {
        return new ResponseModel<>(null, data);
    }

    /**
     * Creates a response model with the given data and message.
     *
     * @param data    the response data
     * @param message the message to include
     * @param <T>     the type of the response data
     * @return a new ResponseModel instance
     */
    public static <T> ResponseModel<T> of(T data, String message) {
        return new ResponseModel<>(message, data);
    }
}
