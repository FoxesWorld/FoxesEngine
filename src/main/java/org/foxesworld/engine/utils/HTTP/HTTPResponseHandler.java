package org.foxesworld.engine.utils.HTTP;

import org.foxesworld.engine.Engine;

import java.io.*;
import java.net.HttpURLConnection;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class HTTPResponseHandler implements AutoCloseable, Flushable {

    private final HttpURLConnection connection;
    private InputStream inputStream;
    private BufferedReader reader;
    private final Charset charset;

    /**
     * Constructor initializes HTTPResponseHandler with the connection and sets charset based on content type.
     *
     * @param connection HttpURLConnection instance
     * @throws IOException if an I/O error occurs
     */
    public HTTPResponseHandler(HttpURLConnection connection) throws IOException {
        this.connection = connection;
        this.inputStream = connection.getInputStream();
        this.charset = getCharsetFromConnection(connection);
        this.reader = new BufferedReader(new InputStreamReader(inputStream, charset));
    }

    /**
     * Reads the full HTTP response body as a String.
     *
     * @return Full response body as String.
     * @throws IOException If an error occurs during reading.
     */
    public String getResponseBody() throws IOException {
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            response.append(line);
            response.append(System.lineSeparator()); // Preserve line breaks
        }
        return response.toString().trim();
    }

    /**
     * Reads the full HTTP response body as a byte array.
     *
     * @return Full response body as byte array.
     * @throws IOException If an error occurs during reading.
     */
    public byte[] getResponseBodyAsBytes() throws IOException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        try (InputStream is = connection.getInputStream()) {
            byte[] data = new byte[1024];
            int nRead;
            while ((nRead = is.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, nRead);
            }
        }
        return buffer.toByteArray();
    }

    /**
     * Flushes any remaining input in the buffer to ensure complete processing.
     *
     * @throws IOException If an error occurs during flushing.
     */
    @Override
    public void flush() throws IOException {
        if (inputStream != null) {
            byte[] buffer = new byte[8192];
            while (inputStream.read(buffer) != -1) {
                // Discard data, essentially draining the input stream
                /*
                * Alternatively, if this method’s purpose is specifically to ensure a response stream has been fully consumed after processing,
                * you could simply document its role as "drain"
                * or remove it entirely if not essential.*/
            }
        }
    }

    /**
     * Closes all resources including input stream and BufferedReader.
     *
     * @throws IOException If an error occurs during closing.
     */
    @Override
    public void close() throws IOException {
        try {
            if (reader != null) {
                reader.close();
            }
        } catch (IOException e) {
            Engine.LOGGER.warn("Error closing BufferedReader: {}", e.getMessage());
        }
        try {
            if (inputStream != null) {
                inputStream.close();
            }
        } catch (IOException e) {
            Engine.LOGGER.warn("Error closing InputStream: {}", e.getMessage());
        }
        connection.disconnect();
    }

    /**
     * Retrieves the charset from the connection's content type or defaults to UTF-8.
     *
     * @param connection HttpURLConnection instance
     * @return Charset based on content type or default UTF-8
     */
    private Charset getCharsetFromConnection(HttpURLConnection connection) {
        String contentType = connection.getContentType();
        if (contentType != null && contentType.contains("charset=")) {
            String charsetName = contentType.split("charset=")[1].trim();
            try {
                return Charset.forName(charsetName);
            } catch (Exception e) {
                Engine.LOGGER.warn("Unsupported charset {}, defaulting to UTF-8", charsetName);
            }
        }
        return StandardCharsets.UTF_8; // Default charset
    }
}
