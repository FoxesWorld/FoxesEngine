package org.foxesworld.engine.utils.HTTP;

@FunctionalInterface
public interface OnSuccess<T> {
    void onSuccess(T response);
}