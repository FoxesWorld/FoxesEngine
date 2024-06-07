package org.foxesworld.engine.utils;

import org.foxesworld.engine.Engine;
import org.foxesworld.engine.gui.components.ComponentAttributes;

import javax.swing.*;

public class IconUtils {

    private  Engine engine;

    public  IconUtils(Engine engine){
        this.engine = engine;
    }

    public ImageIcon getIcon(ComponentAttributes componentAttributes){
        ImageIcon icon = null;
        String iconPath = componentAttributes.getImageIcon();
        if(iconPath.endsWith(".png") || iconPath.endsWith(".jpg")) {
            icon = new ImageIcon(this.engine.getImageUtils().getScaledImage(this.engine.getImageUtils().getLocalImage(componentAttributes.getImageIcon()), componentAttributes.getIconWidth(), componentAttributes.getIconHeight()));
            if (componentAttributes.getBorderRadius() != 0) {
                icon = new ImageIcon(this.engine.getImageUtils().getRoundedImage(icon.getImage(), componentAttributes.getBorderRadius()));
            }
        } else if(iconPath.endsWith(".svg")) {
            Engine.LOGGER.debug("Missing Batik!!!");
            //icon = convertSVGToImage(ComponentFactory.class.getClassLoader().getResourceAsStream(iconPath), componentAttributes.getIconWidth(), componentAttributes.getIconHeight());
        } else if(iconPath.endsWith(".gif")) {
        }

        return icon;
    }

    /*
    private static ImageIcon convertSVGToImage(InputStream svgInputStream, int width, int height) {
        try {
            String parser = XMLResourceDescriptor.getXMLParserClassName();
            SAXSVGDocumentFactory factory = new SAXSVGDocumentFactory(parser);
            Document svgDocument = factory.createDocument(null, svgInputStream);

            PNGTranscoder transcoder = new PNGTranscoder();
            transcoder.addTranscodingHint(PNGTranscoder.KEY_WIDTH, (float) width);
            transcoder.addTranscodingHint(PNGTranscoder.KEY_HEIGHT, (float) height);
            TranscoderInput input = new TranscoderInput(svgDocument);

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            TranscoderOutput output = new TranscoderOutput(outputStream);

            transcoder.transcode(input, output);

            ByteArrayInputStream imageInputStream = new ByteArrayInputStream(outputStream.toByteArray());
            BufferedImage image = ImageIO.read(imageInputStream);

            return new ImageIcon(image);
        } catch (IOException | TranscoderException e) {
            e.printStackTrace();
            return null;
        }
    }
    */
}
