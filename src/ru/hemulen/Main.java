package ru.hemulen;

import ru.hemulen.thread.Processing;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * Приложение читает из базы данных адаптера СМЭВ входящие с ЕПГУ заявления, формирует на них ответы в соответствии
 * с определенными критериями отправляет ответы адаптер СМЭВ
 */
public class Main {
    public static void main(String[] args) {
        Properties props = new Properties();
        try {
            props.load(new FileInputStream("./config/config.ini"));
        } catch (IOException e) {
            System.out.println("Не удалось загрузить файл конфигурации");
            e.printStackTrace();
            System.exit(1);
        }
        // Создаем и запускаем процесс обработки заявлений
        Processing processing = new Processing(props);
        processing.run();
    }
}
