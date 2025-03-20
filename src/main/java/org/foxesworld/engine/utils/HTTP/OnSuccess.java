package org.foxesworld.engine.utils.HTTP;

@FunctionalInterface
@Deprecated
public interface OnSuccess<T> {
    void onSuccess(T response);
}