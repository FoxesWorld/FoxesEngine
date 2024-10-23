package org.foxesworld.engine.utils.HTTP;

public interface OnSuccess<T> {
    void onSuccess(T response);
}