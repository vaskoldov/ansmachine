package ru.hemulen.db;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.Properties;

/**
 * Класс выполняет выборки и другие операции с данными в БД адаптера
 */
public class AdapterDB {
    private static Logger LOG = LoggerFactory.getLogger(AdapterDB.class.getName());
    private Connection conn;

    public AdapterDB(Properties props) {
        // Подключаемся к базе адаптера
        String pgURL = String.format("jdbc:postgresql://%s:%s/%s", props.getProperty("DB_HOST"), props.getProperty("DB_PORT"), props.getProperty("DB_NAME"));
        LOG.info(String.format("Подключение к БД адаптера: %s", pgURL));
        try {
            Class.forName("org.postgresql.Driver");
            conn = DriverManager.getConnection(pgURL, props.getProperty("DB_USER"), props.getProperty("DB_PASS"));
            LOG.info("Подключение установлено");
        } catch (ClassNotFoundException e) {
            LOG.error("Не найден драйвер PostgreSQL");
            LOG.error(e.getMessage());
            System.exit(2);
        } catch (SQLException e) {
            LOG.error("Не удалось установить соединение с базой данных адаптера");
            LOG.error(e.getMessage());
            System.exit(2);
        }
    }

    public ResultSet getApplications(String sql) {
        try {
            Statement stmt = conn.createStatement();
            LOG.info("Выполнен запрос к базе данных");
            return stmt.executeQuery(sql);
        } catch (SQLException e) {
            LOG.error("Ошибка выполнения SQL запроса");
            LOG.error(e.getMessage());
            return null;
        }
    }
}
