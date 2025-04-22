package org.foxesworld.engine.fileLoader;

import java.io.File;

/**
 * Интерфейс для проверки валидности локальных файлов.
 */
public interface IFileValidator {
    /**
     * Проверка файла на соответствие ожидаемым параметрам.
     *
     * @param localFile   локальный файл
     * @param expectedHash ожидаемый хэш файла
     * @param expectedSize ожидаемый размер файла
     * @return true, если файл не соответствует ожиданиям (невалиден), иначе false
     */
    boolean isInvalidFile(File localFile, String expectedHash, long expectedSize);

    /**
     * Проверяет, является ли файл валидным.
     *
     * @param localFile    локальный файл
     * @param expectedHash ожидаемый MD5-хэш файла
     * @param expectedSize ожидаемый размер файла в байтах
     * @return true, если файл валиден; иначе false
     */
    default boolean isValidFile(File localFile, String expectedHash, long expectedSize) {
        return !isInvalidFile(localFile, expectedHash, expectedSize);
    }
}
