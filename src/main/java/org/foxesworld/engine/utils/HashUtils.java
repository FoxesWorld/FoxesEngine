package org.foxesworld.engine.utils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Formatter;

@SuppressWarnings("unused")
public final class HashUtils {
    public static String md5(String filename) {
        if (new File(filename).isDirectory()) {
            return "IDE";
        }
        FileInputStream fis = null;
        FilterInputStream dis = null;
        BufferedInputStream bis = null;
        Formatter formatter = null;
        try {
            MessageDigest messagedigest = MessageDigest.getInstance("MD5");
            fis = new FileInputStream(filename);

            bis = new BufferedInputStream(fis);
            dis = new DigestInputStream(bis, messagedigest);
            while ((dis).read() != -1) {
            }
            byte[] abyte0 = messagedigest.digest();
            formatter = new Formatter();
            for (byte byte0 : abyte0) {
                formatter.format("%02x", byte0);
            }
            return formatter.toString();

        } catch (IOException | NoSuchAlgorithmException e) {
            return "0";
        } finally {
            try {
                assert fis != null;
                fis.close();
            } catch (IOException ignored) {
            }
            try {
                assert dis != null;
                dis.close();
            } catch (IOException ignored) {
            }
            try {
                bis.close();
            } catch (IOException ignored) {
            }
            try {
                assert formatter != null;
                formatter.close();
            } catch (Exception ignored) {
            }
        }
    }

    public static String sha1String(String input)
    {
        String hash = null;
        try
        {
            MessageDigest m = MessageDigest.getInstance("SHA1");
            byte[] result = m.digest(input.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : result) {
                sb.append(Integer.toString((b & 0xff) + 0x100, 16).substring(1));
            }
            hash = sb.toString();
        } catch (Exception e)
        {
            e.printStackTrace();
        }

        return hash;
    }

    public static String calculateSHA1(String filePath) {
        Path path = Paths.get(filePath);
        MessageDigest md = null;
        byte[] fileBytes;
        try {
            md = MessageDigest.getInstance("SHA-1");
            fileBytes = Files.readAllBytes(path);
        } catch (NoSuchAlgorithmException | IOException e) {
            throw new RuntimeException(e);
        }

        md.update(fileBytes);
        byte[] digest = md.digest();
        StringBuilder sb = new StringBuilder();
        for (byte b : digest) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}

