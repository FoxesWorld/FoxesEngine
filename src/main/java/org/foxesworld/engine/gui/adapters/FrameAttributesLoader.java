package org.foxesworld.engine.gui.adapters;

import org.foxesworld.engine.gui.components.Attributes;

public interface FrameAttributesLoader {
    Attributes load(String framePath);
}