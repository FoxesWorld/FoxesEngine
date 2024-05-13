package org.foxesworld.engine.utils;

import org.foxesworld.engine.Engine;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import static org.foxesworld.engine.utils.HashUtils.sha1String;

public class ImageUtils {
    private static final Map<String, BufferedImage> imgCache = new HashMap<>();

    public static BufferedImage getLocalImage(String name) {
        if (imgCache.containsKey(name)) {
            return imgCache.get(name);
        }

        try {
            InputStream inputStream = ImageUtils.class.getClassLoader().getResourceAsStream(name);
            if (inputStream != null) {
                BufferedImage img = ImageIO.read(inputStream);
                imgCache.put(name, img);
                return img;
            } else {
                Engine.LOGGER.error("Failed to open local image: {}", name);
                return new BufferedImage(9, 9, BufferedImage.TYPE_INT_ARGB);
            }
        } catch (IOException e) {
            Engine.LOGGER.error("Failed to open local image: {}", name, e);
            return new BufferedImage(9, 9, BufferedImage.TYPE_INT_ARGB);
        }
    }


    public static BufferedImage base64ToBufferedImage(String base64Image) {
        try {
            byte[] imageBytes = Base64.getDecoder().decode(base64Image);
            ByteArrayInputStream bis = new ByteArrayInputStream(imageBytes);
            BufferedImage bufferedImage = ImageIO.read(bis);
            bis.close();
            return bufferedImage;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static BufferedImage loadImageFromUrl(String imageUrl) {
        try {
            if (!isValidUrl(imageUrl)) {
                return null;
            }

            URL url = new URL(imageUrl);
            return ImageIO.read(url);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static BufferedImage getCachedUrlImg(String imageUrl, String cachePath, BufferedImage ifNotFound) {
        try {
            String cacheKey = sha1String(imageUrl);

            if (imgCache.containsKey(cacheKey)) {
                return imgCache.get(cacheKey);
            }

            String cacheFilePath = "cache" + File.separator + cachePath + File.separator + cacheKey;
            File cacheFile = new File(cacheFilePath);

            if (!cacheFile.exists()) {
                BufferedImage image = loadImageFromUrl(imageUrl);
                if (image != null) {
                    imgCache.put(cacheKey, image);
                    cacheFile.getParentFile().mkdirs();
                    ImageIO.write(image, "png", cacheFile);
                    Engine.LOGGER.info("Image downloaded and cached: {}", imageUrl);
                    return image;
                }
            } else {
                BufferedImage image = ImageIO.read(cacheFile);
                imgCache.put(cacheKey, image);
                return image;
            }
        } catch (IOException e) {
            Engine.LOGGER.error("Error loading image from URL: {}", imageUrl, e);
        }
        return ifNotFound;
    }

    private static String getFileNameFromUrl(String urlString) {
        String[] parts = urlString.split("/");
        return parts[parts.length - 1];
    }

    private static boolean isValidUrl(String urlString) {
        try {
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("HEAD");
            int responseCode = connection.getResponseCode();
            return responseCode == HttpURLConnection.HTTP_OK;
        } catch (IOException e) {
            return false;
        }
    }
    public static BufferedImage getRoundedImage(Image image, int cornerRadius) {
        int width = image.getWidth(null);
        int height = image.getHeight(null);

        BufferedImage roundedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = roundedImage.createGraphics();

        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        Shape shape;
        if (width == height) {
            shape = new Ellipse2D.Float(0, 0, width, height);
        } else {
            shape = new RoundRectangle2D.Float(0, 0, width, height, cornerRadius, cornerRadius);
        }
        g2.setClip(shape);
        g2.drawImage(image, 0, 0, null);
        g2.dispose();

        return roundedImage;
    }


    public static Image getRoundedImage(Image image, int width, int height) {
        BufferedImage roundedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = roundedImage.createGraphics();
        g2.setClip(new Ellipse2D.Float(0, 0, width, height));
        g2.drawImage(image, 0, 0, width, height, null);
        g2.dispose();
        return roundedImage;
    }

    public static Image getScaledImage(Image srcImg, int w, int h) {
        BufferedImage resizedImg = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = resizedImg.createGraphics();

        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        if (w > srcImg.getWidth(null) || h > srcImg.getHeight(null)) {
            g2.drawImage(srcImg, 0, 0, w, h, 0, 0, srcImg.getWidth(null), srcImg.getHeight(null), null);
        } else {
            g2.drawImage(srcImg, 0, 0, w, h, null);
        }
        g2.dispose();
        return resizedImg;
    }

    public static Image getScaledImage(Image srcImg, double scale) {
        int w = (int) (srcImg.getWidth(null) * scale);
        int h = (int) (srcImg.getHeight(null) * scale);

        BufferedImage resizedImg = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = resizedImg.createGraphics();

        AffineTransform at = AffineTransform.getScaleInstance(scale, scale);
        g2.drawRenderedImage(toBufferedImage(srcImg), at);

        g2.dispose();
        return resizedImg;
    }


    public static BufferedImage genButton(int w, int h, BufferedImage img) {
        if (w <= 0 || h <= 0) {
            throw new IllegalArgumentException("Width and height must be greater than zero.");
        }

        BufferedImage res = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        BufferedImage left = img.getSubimage(0, 0, img.getWidth() / 3, img.getHeight());
        BufferedImage center = img.getSubimage(img.getWidth() / 3, 0, img.getWidth() / 3, img.getHeight());
        BufferedImage right = img.getSubimage(img.getWidth() / 3 * 2, 0, img.getWidth() / 3, img.getHeight());

        Graphics2D g = res.createGraphics();
        g.drawImage(left, 0, 0, left.getWidth(), h, null);
        g.drawImage(center, left.getWidth(), 0, w - left.getWidth() - right.getWidth(), h, null);
        g.drawImage(right, w - right.getWidth(), 0, right.getWidth(), h, null);
        g.dispose();

        return res;
    }

    public static boolean contains(int x2, int y2, int xx, int yy, int w, int h) {
        return x2 >= xx && y2 >= yy && x2 < xx + w && y2 < yy + h;
    }

    public static BufferedImage fill(BufferedImage texture, int w, int h) {
        int sizex = texture.getWidth();
        int sizey = texture.getHeight();
        BufferedImage img = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = img.createGraphics();

        for (int x2 = 0; x2 <= w / sizex; ++x2) {
            for (int y2 = 0; y2 <= h / sizey; ++y2) {
                g2d.drawImage(texture, x2 * sizex, y2 * sizey, null);
            }
        }
        g2d.dispose();
        return img;
    }

    private String getImageHash(BufferedImage image) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ImageIO.write(image, "png", outputStream);
            byte[] data = outputStream.toByteArray();
            byte[] hashBytes = digest.digest(data);
            StringBuilder hashBuilder = new StringBuilder();
            for (byte b : hashBytes) {
                hashBuilder.append(String.format("%02x", b));
            }
            return hashBuilder.toString();
        } catch (NoSuchAlgorithmException | IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    public static BufferedImage fillHoriz(BufferedImage texture, int w, int h) {
        int sizex = texture.getWidth();
        BufferedImage img = new BufferedImage(w, h, 2);
        for (int x2 = 0; x2 <= w / sizex; ++x2) {
            img.getGraphics().drawImage(texture, x2 * sizex, 0, sizex, texture.getHeight(), null);
        }
        return img;
    }

    public static BufferedImage blurImage(BufferedImage image, float ninth) {
        float[] blurKernel = new float[]{ninth, ninth, ninth, ninth, ninth, ninth, ninth, ninth, ninth};
        HashMap<RenderingHints.Key, Object> map = new HashMap<>();
        map.put(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        map.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        map.put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        RenderingHints hints = new RenderingHints(map);
        ConvolveOp op = new ConvolveOp(new Kernel(3, 3, blurKernel), 1, hints);
        return op.filter(image, null);
    }
    public static BufferedImage screenComponent(JComponent c) {
        BufferedImage img = new BufferedImage(c.getWidth(), c.getHeight(), 2);
        Graphics2D g = img.createGraphics();
        c.paint(g);
        g.dispose();
        return img;
    }

    private static BufferedImage toBufferedImage(Image img) {
        if (img instanceof BufferedImage) {
            return (BufferedImage) img;
        }

        BufferedImage bufferedImage = new BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB);
        Graphics g = bufferedImage.getGraphics();
        g.drawImage(img, 0, 0, null);
        g.dispose();

        return bufferedImage;
    }

    public static void setRenderingHints(Graphics2D g2d) {
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
    }

    public static void drawWithTransparency(Graphics2D g2d, Image image, int x, int y, int width, int height, int transparency) {
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
        g2d.drawImage(image, x, y, width, height, null);
    }


    public static BufferedImage getByIndex(BufferedImage all, int d, int i) {
        return all.getSubimage(d * i, 0, d, d);
    }

    public static BufferedImage getByIndexCR(BufferedImage all, int d, int row, int i) {
        return all.getSubimage(d * i, row * i, d, d);
    }

    public static BufferedImage[] spriteCollsRows(BufferedImage img, int colls, int rows, int width, int height) {
        BufferedImage[] spritesOut = new BufferedImage[rows * colls];
        int i = 0;
        int j = 0;
        for (i = 0; i < rows; ++i) {
            for (j = 0; j < colls; ++j) {
                spritesOut[i * colls + j] = img.getSubimage(j * width, i * height, width, height);
            }
        }
        return spritesOut;
    }
}