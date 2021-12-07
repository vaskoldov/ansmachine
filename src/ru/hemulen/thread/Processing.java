package ru.hemulen.thread;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.hemulen.db.AdapterDB;
import ru.hemulen.object.ActivateNumber;
import ru.hemulen.object.Device;
import ru.hemulen.object.PhoneInfo;
import ru.hemulen.object.RegM2M;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.ListIterator;
import java.util.Map;
import java.util.Properties;

/**
 * Процесс AnsweringMachineStandby выполняется в бесконечном цикле, опрашивая БД адаптера и обрабатывая полученные заявления
 */
public class Processing extends Thread {
    private Logger LOG = LoggerFactory.getLogger(Processing.class.getName());
    private AdapterDB adapterDB;
    private boolean isRunnable = true;
    private Map<String, PhoneInfo> phoneInfoMap;
    private String phoneBasePath;
    private String adapterPath;
    private String sql = ""; // SQL-запрос, которым из базы адаптера извлекаются заявления для обработки
    private String processingMode; // Режим работы автоответчика (SINGLE - разовый проход, LOOP - бесконечный цикл)
    private FileWriter activateNumberLog;
    private FileWriter regM2MLog;
    private int pause;
    private Properties props;

    public Processing(Properties props) {
        // Задаем имя потока
        setName("AnsweringMachineStandby");
        LOG.info("Создается поток Processing");
        this.props = props; // Сохраняем конфигурацию для последующих вызовов конструкторов ActivateNumber и RegM2M
        // Определяем режим работы автоответчика
        processingMode = props.getProperty("PROCESSING_MODE");
        // Получаем путь, по которому будут сохраняться файлы с ответами
        adapterPath = props.getProperty("ADAPTER_PATH");
        // Получаем SQL-запрос для выборки из БД адаптера заявлений на обработку
        try {
            BufferedReader br = new BufferedReader(new FileReader(props.getProperty("SQL_PATH")));
            String line;
            while ((line = br.readLine()) != null) {
                sql += line;
                sql += " "; //Добавляем пробел на случай, если запрос написан в несколько строк
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Получаем путь, по которому будут сохраняться логи обработки заявлений на ВС "Регистрация номеров M-to-M посредством ЕПГУ"
        // В имя файла добавляется дата обработки заявления. Каждый день пишутся новые логи.
        String localDate = "_" + LocalDate.now().toString() + ".csv";
        try {
            // Создаем лог для обработанных заявлений на ВС "Регистрация корпоративного номера телефона"
            Path activateNumberLogPath = Paths.get(this.props.getProperty("ACTIVATE_NUMBER_LOG") + localDate);
            // Если файл не существует, то создаем его
            if (!activateNumberLogPath.toFile().exists()) {
                File activateNumberLogFile = new File(activateNumberLogPath.toString());
            }
            // Создаем FileWriter для записи в лог
            activateNumberLog = new FileWriter(activateNumberLogPath.toString(), true);

            // Создаем лог для обработанных заявлений на ВС "Регистрация номеров M-to-M посредством ЕПГУ"
            Path regM2MLogPath = Paths.get(this.props.getProperty("REG_M2M_LOG") + localDate);
            // Если файл не существует, то создаем его
            if (!regM2MLogPath.toFile().exists()) {
                File regM2MLofFile = new File(regM2MLogPath.toString());
            }
            // Создаем FileWriter для записи в лог
            regM2MLog = new FileWriter(regM2MLogPath.toString(), true);
        } catch (IOException e) {
            e.printStackTrace();
        }
        // Получаем время задержки перед отправкой очередного запроса к базе данных адаптера
        pause = Integer.parseInt(props.getProperty("PAUSE"));
        // Создаем класс для работы с базой данных адаптера
        adapterDB = new AdapterDB(props);
        // Получаем из настроек путь к CSV-файлу с базой номеров оператора
        phoneBasePath = props.getProperty("PHONE_BASE");
        // Создаем массив с номерами телефонов для проверок заявлений
        phoneInfoMap = new HashMap<>();
        loadPhoneBase();
    }

    @Override
    public void run() {
        ResultSet applications;
        while (isRunnable) {
            applications = adapterDB.getApplications(sql);
            while (true) {
                try {
                    if (!applications.next()) {
                        // Заявления в выборке закончились
                        try {
                            // Записываем логи на диск
                            activateNumberLog.close();
                            regM2MLog.close();
                            if (processingMode.compareTo("SINGLE") == 0) {
                                // Завершаем работу автоответчика
                                isRunnable = false;
                                break;
                            } else {
                                // Ждем, когда адаптер отправит сформированные ответы
                                sleep(this.pause);
                                // Обновляем базу телефонных номеров
                                loadPhoneBase();
                                // Подключаем логи (и обновляем их, если изменилась дата)
                                setupLogs();
                                // И продолжаем бесконечный цикл
                            }
                        } catch (IOException e) {
                            LOG.error(e.getMessage());
                        } catch (InterruptedException e) {
                            LOG.error(e.getMessage());
                        }
                        applications = adapterDB.getApplications(sql); // Получаем новую порцию заявлений из БД
                        continue;
                    }
                    // Считываем значения полей в выборке заявлений из БД адаптера
                    String clientID = applications.getString(1);
                    String messageID = applications.getString(2);
                    String mnemonic = applications.getString(3);
                    String inquiry = applications.getString(4);
                    String xmlContent = applications.getString(5);
                    // Вызываем обработчик заявления
                    processApplication(clientID, messageID, mnemonic, inquiry, xmlContent);
                } catch (SQLException | IllegalStateException e) {
                    LOG.error(e.getMessage());
                }
            }
        }
    }

    /**
     * Метод получает атрибуты записи с запросом,
     * определяет ВС, по которому пришел запрос,
     * вызывает XML-парсер и вытягивает нужные элементы из запроса,
     * определяет статус ответа,
     * формирует ответ,
     * записывает ClientMessage с ответом в каталог integration/files соответствующей системы,
     * записывает результат обработки заявления в лог
     *
     * @param clientID
     * @param mnemonic
     * @param inquiry
     * @param xmlContent
     */
    private void processApplication(String clientID, String messageID, String mnemonic, String inquiry, String xmlContent) throws IllegalStateException {
        String vsName;
        switch (inquiry) {
            case "urn://gosuslugi/activate-number/1.0.1":
                vsName = "ActivateNumber"; // ВС "Регистрация корпоративного номера телефона"
                processActivateNumber(clientID, messageID, mnemonic, xmlContent, (HashMap) phoneInfoMap);
                LOG.info(String.format("Обработано заявление \"Регистрация корпоративного номера телефона\" из запроса с messageID %s", messageID));
                break;
            case "urn://gosuslugi/reg-m2m/1.0.1":
                vsName = "RegM2M"; // ВС "Регистрация номеров M-to-M посредством ЕПГУ"
                processRegM2M(clientID, messageID, mnemonic, xmlContent);
                LOG.info(String.format("Обработано заявление \"Регистрация номеров M-to-M посредством ЕПГУ\" из запроса с messageID %s", messageID));
                break;
            default:
                LOG.warn(String.format("Unexpected value: %s", inquiry));
        }
    }

    /**
     * Метод обрабатывает заявления по ВС "Регистрация корпоративного номера телефона"
     *
     * @param clientID   - client_id запроса с заявлением
     * @param mnemonic   - мнемоника ИС, от которой должен быть отправлен ответ
     * @param xmlContent - содержимое XML запроса
     */
    private void processActivateNumber(String clientID, String messageID, String mnemonic, String xmlContent, HashMap phoneInfoMap) {
        // Создаем объект с атрибутами заявления
        ActivateNumber application = new ActivateNumber(xmlContent, this.props);
        // Проверяем наличие номера телефона, указанного в заявлении, номеру в базе операторов и совпадение операторов
        application.check(mnemonic, phoneInfoMap);
        // Формируем сообщение адаптера ClientMessage с ответом на заявление
        String response = application.getResponse(mnemonic, clientID);
        // Сохраняем строку с ответом в файловом хранилище адаптера
        File responseFile = Paths.get(this.adapterPath, mnemonic, "out", messageID + ".xml").toFile();
        try {
            OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(responseFile), "UTF-8");
            writer.write(response);
            writer.close();
        } catch (IOException e) {
            LOG.error(e.getMessage());
        }
        // Записываем результат обработки заявления в лог
        String result = "Регистрация корпоративного номера телефона;" +
                messageID + ";" +
                application.getOrderId() + ";" +
                application.getLastName() + ";" +
                application.getFirstName() + ";" +
                application.getMiddleName() + ";" +
                application.getBirthDate() + ";" +
                application.getDocType() + ";" +
                application.getDocNumber() + ";" +
                application.getDocDate() + ";" +
                application.getOperatorName() + ";" +
                application.getCorpPhone() + ";" +
                application.getClearPhone() + ";" +
                application.getInn() + ";" +
                application.getResult() + ";" +
                application.getComment() + "\n";
        try {
            activateNumberLog.write(result);
        } catch (IOException e) {
            LOG.error(e.getMessage());
        }
    }

    /**
     * Метод обрабатывает заявления по ВС "Регистрация номеров M-to-M посредством ЕПГУ"
     *
     * @param clientID   - client_id запроса с заявлением
     * @param mnemonic   - мнемоника ИС, от которой должен быть отправлен ответ
     * @param xmlContent - содержимое XML запроса
     */
    private void processRegM2M(String clientID, String messageID, String mnemonic, String xmlContent) {
        // Парсим XML заявления и создаем объект RegM2M
        RegM2M regM2M = new RegM2M(xmlContent, props);
        // Проверяем телефоны из заявления по базе оператора
        regM2M.check(mnemonic, (HashMap) phoneInfoMap);
        // Формируем сообщение адаптера ClientMessage с ответом на заявление
        String response = regM2M.getResponse(mnemonic, clientID);
        // Сохраняем строку с ответом в файловом хранилище адаптера
        File responseFile = Paths.get(this.adapterPath, mnemonic, "out", messageID + ".xml").toFile();
        try {
            OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(responseFile), "UTF-8");
            writer.write(response);
            writer.close();
        } catch (IOException e) {
            LOG.error(e.getMessage());
        }
        // Записываем результат обработки заявления в лог
        for (ListIterator<Device> iter = regM2M.getDevices().listIterator(); iter.hasNext(); ) {
            Device currDevice = iter.next();
            String result = "Регистрация номеров M-to-M посредством ЕПГУ;" +
                    messageID + ";" +
                    regM2M.getOrderId() + ";" +
                    regM2M.getInn() + ";" +
                    regM2M.getOperatorName() + ";" +
                    regM2M.getOrgName() + ";" +
                    (currDevice.getDeviceAddress().length() > 0 ? currDevice.getDeviceAddress() : currDevice.getOrgAddress()) + ";" +
                    currDevice.getPhoneNumber() + ";" +
                    currDevice.getClearPhoneNumber() + ";" +
                    currDevice.getDeviceNumber() + ";" +
                    currDevice.getDeviceType() + ";" +
                    currDevice.getDeviceName() + ";" +
                    currDevice.getMfrName() + ";" +
                    currDevice.getCheckResult() + ";" +
                    currDevice.getComment() + ";" +
                    regM2M.getCheckResult() + ";" +
                    regM2M.getComment() + "\n";
            try {
                regM2MLog.write(result);
            } catch (IOException e) {
                LOG.error(e.getMessage());
            }

        }
    }

    /**
     * Метод считывает файл с базой телефонов оператора и заполняет HashMap phoneInfoMap с телефонами в памяти
     * Новые номера добавляются, имена операторов для существующих номеров перезаписываются.
     *
     * @return
     */
    private void loadPhoneBase() {
        try {
            BufferedReader br = new BufferedReader(new FileReader(phoneBasePath));
            String line;
            int i = 1; // Определяем номер строки, в которой может быть найден некорректный оператор
            while ((line = br.readLine()) != null) {
                String[] elements = line.split(";");
                PhoneInfo pi = new PhoneInfo();
                pi.operatorName = elements[0];
                switch (pi.operatorName) {
                    case "Таттелеком":
                        pi.operatorMnemonic = "162101";
                        break;
                    case "Летай":
                        pi.operatorMnemonic = "168201";
                        break;
                    default:
                        pi.operatorMnemonic = "UNKNOWN";
                        LOG.warn(String.format("В списке номеров телефонов в строке %d найден неизвестный оператор %s", i, elements[0]));
                        break;
                }
                pi.phoneNumber = elements[1];
                phoneInfoMap.put(pi.phoneNumber, pi);
                i++;
            }
            LOG.info(String.format("Загружено %d записей из базы оператора", i));
        } catch (IOException e) {
            LOG.error(e.getMessage());
            System.out.println("Не удалось прочитать файл с номерами телефонов");
            System.exit(1);
        }
    }

    /**
     * Метод вызывается каждый раз перед обработкой очередной порции заявлений.
     * Если при этом происходит смена текущей даты, то логи получают новые имена, включающие текущую дату
     */
    private void setupLogs() {
        // Получаем путь, по которому будут сохраняться логи обработки заявлений на ВС "Регистрация номеров M-to-M посредством ЕПГУ"
        // В имя файла добавляется дата обработки заявления. Каждый день пишутся новые логи.
        String localDate = "_" + LocalDate.now().toString() + ".cvs";
        try {
            // Создаем лог для обработанных заявлений на ВС "Регистрация корпоративного номера телефона"
            Path activateNumberLogPath = Paths.get(props.getProperty("ACTIVATE_NUMBER_LOG") + localDate);
            // Если файл не существует, то создаем его
            if (!activateNumberLogPath.toFile().exists()) {
                File activateNumberLogFile = new File(activateNumberLogPath.toString());
            }
            // И теперь создаем FileWriter для записи в лог
            activateNumberLog = new FileWriter(activateNumberLogPath.toString(), true);

            // Создаем лог для обработанных заявлений на ВС "Регистрация номеров M-to-M посредством ЕПГУ"
            Path regM2MLogPath = Paths.get(props.getProperty("REG_M2M_LOG") + localDate);
            // Если файл не существует, то создаем его
            if (!regM2MLogPath.toFile().exists()) {
                File regM2MLofFile = new File(regM2MLogPath.toString());
            }
            regM2MLog = new FileWriter(regM2MLogPath.toString(), true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
