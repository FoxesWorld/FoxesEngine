package org.foxesworld.engine.utils.HTTP;

@FunctionalInterface
@Deprecated
public interface OnFailure {
    void onFailure(Exception e);
}