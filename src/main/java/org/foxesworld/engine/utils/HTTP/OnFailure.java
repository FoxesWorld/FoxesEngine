package org.foxesworld.engine.utils.HTTP;

@FunctionalInterface
public interface OnFailure {
    void onFailure(Exception e);
}