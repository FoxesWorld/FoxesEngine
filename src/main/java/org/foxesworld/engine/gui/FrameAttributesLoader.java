package org.foxesworld.engine.gui;

import org.foxesworld.engine.gui.components.Attributes;
import org.foxesworld.engine.gui.components.frame.FrameAttributes;

interface FrameAttributesLoader {
    Attributes load(String framePath);
}