package org.foxesworld.engine.fileLoader;

/**
 * Интерфейс для получения списка атрибутов файлов.
 */
public interface IFileFetcher {
    /**
     * Получение списка атрибутов файлов для загрузки.
     *
     * @param client        название клиента
     * @param version       версия клиента
     * @param platformCode  код платформы (например, 1 для Windows, 2 для Mac и т.д.)
     * @return CompletableFuture с массивом атрибутов файлов
     */
    java.util.concurrent.CompletableFuture<FileAttributes[]> fetchDownloadList(String client, String version, int platformCode);
}
