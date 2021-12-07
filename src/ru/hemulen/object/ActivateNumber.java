package ru.hemulen.object;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.xpath.*;
import java.io.*;
import java.util.HashMap;
import java.util.Properties;
import java.util.UUID;

public class ActivateNumber {
    private static Logger LOG = LoggerFactory.getLogger(ActivateNumber.class.getName());

    private String orderId;     // Номер заявления с ЕПГУ

    private String orderDate;   // Дата заявления
    private String lastName;    // Фамилия
    private String firstName;   // Имя
    private String middleName;  // Отчество
    private String birthDate;   // Дата рождения
    private String docType;     // Тип ДУЛ
    private String docNumber;   // Серия и номер ДУЛ
    private String docDate;     // Дата выдачи ДУЛ
    private String operatorName;// Наименование оператора связи
    private String operatorId;  // Идентификатор оператора связи
    private String operatorIdAdd;   // Дополнительный идентификатор оператора связи
    private String routeNumber; // Мнемоника ИС получателя
    private String corpPhone;   // Номер корпоративного телефона
    private String clearPhone;  // Номер корпоративного телефона, очищенный от спецсимволов и обрезанный до 10 знаков справа
    private String inn;         // ИНН организации, на которую оформлен телефон
    private String orgName;     // Наименование организации, на которую оформлен телефон
    private String action;      // Код действия с заявлением
    private String note;        // Комментарий к действию с заявлением
    private String result;      // Результат проверки заявления по базе телефонов оператора
    private String comment;     // Комментарий к результату проверки заявления по базе телефонов оператора
    private String ActivateNumberStylesheet; // XSL-файл с преобразованием запроса в ответ
    private Document application;   // DOM-объект с заявлением
    private Element root;       // Корень XML-документа
    private XPathFactory xPathFactory;
    private XPath xPath;

    public ActivateNumber(String xmlString, Properties props) {
        // Определяем из конфигурации файл для формирования ответа на заявление
        this.ActivateNumberStylesheet = props.getProperty("ACTIVATE_NUMBER_STYLESHEET");
        // Парсим xml заявления и достаем из него все элементы
        try {
            xPathFactory = XPathFactory.newInstance();
            xPath = xPathFactory.newXPath();
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            InputSource is = new InputSource(new StringReader(xmlString));
            application = builder.parse(is);
            root = application.getDocumentElement();
        } catch (ParserConfigurationException | IOException | SAXException e) {
            LOG.error(e.getMessage());
        }
        Element root = application.getDocumentElement();
        orderId = getString("orderId");
        orderDate = getString("orderDate");
        lastName = getString("lastname");
        firstName = getString("firstname");
        middleName = getString("middleName");
        birthDate = getString("birthDate");
        docType = getString("docType");
        docNumber = getString("docNumber");
        docDate = getString("docDate");
        operatorName = getString("operatorName");
        operatorId = getString("operatorId");
        operatorIdAdd = getString("operatorIdAdd");
        routeNumber = getString("routeNumber");
        corpPhone = getString("corpPhone");
        // Очищаем номер телефона от скобок и кода страны
        clearPhone = corpPhone.replaceAll("[^0-9]", "");
        // Извлекаем последние 10 разрядов, чтобы отсечь 7 или 8 в начале номера
        if (clearPhone.length() >= 10) {
            clearPhone = clearPhone.substring(clearPhone.length() - 10);
        } // Если номер короче 10 разрядов, то он не будет найден в базе оператора - оставляем его как есть
        inn = getString("inn");
        orgName = getString("orgName");
        action = getString("action");
        note = getString("note");
    }

    public String getResponse(String mnemonic, String replyTo) {
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        File stylesheetFile = new File(this.ActivateNumberStylesheet);
        StreamSource stylesheetSource = new StreamSource(stylesheetFile);
        try {
            Transformer transformer = transformerFactory.newTransformer(stylesheetSource);
            transformer.setParameter("Mnemonic", mnemonic);
            transformer.setParameter("СlientID", UUID.randomUUID().toString());
            transformer.setParameter("ReplyTo", replyTo);
            transformer.setParameter("Status", this.result);
            transformer.setParameter("OrderID", this.orderId);
            transformer.setParameter("Comment", this.comment);

            Source source = new DOMSource(this.application);
            StringWriter sw = new StringWriter();
            Result result = new StreamResult(sw);
            transformer.transform(source, result);
            return sw.toString();
        } catch (TransformerException e) {
            LOG.error(e.getMessage());
            return null;
        }
    }

    /**
     * Метод возвращает значение элемента, имя которого передано в параметре tagName
     * @param tagName - Локальное имя элемента (без префикса namespase)
     * @return - Значение элемента
     */
    private String getString(String tagName) {
        String query = String.format("//*[local-name()='%s']/text()", tagName);
        try {
            XPathExpression exp = xPath.compile(query);
            String result = (String) exp.evaluate(root, XPathConstants.STRING);
            return result;
        } catch (XPathExpressionException e) {
            return null;
        }
    }

    /**
     * Метод проверяет наличие номера телефона в базе оператора и совпадение мнемоники, на которую пришло заявление,
     * мнемонике (оператору), которому принадлежит номер по базе оператора.
     * Если все совпадает, то члену класса ActivateNumber result присваивается значение "3" (Услуга оказана).
     * Иначе члену класса ActivateNumber result присваивается значение "4" (Отказано в предоставлении услуги).
     *
     * @param mnemonic     - мнемоника ИС УВ из результатов запроса к базе данных адаптера
     * @param phoneInfoMap - база телефонов оператора
     */
    public void check(String mnemonic, HashMap phoneInfoMap) {
        if (phoneInfoMap.containsKey(clearPhone)) {
            // Если номер найден в базе оператора, то проверяем соответствие оператора, которому направлено заявление,
            // оператору из базы телефонов оператора
            PhoneInfo pi = (PhoneInfo) phoneInfoMap.get(clearPhone);
            if (pi.operatorMnemonic.compareTo(mnemonic) != 0) {
                result = "4"; // Отказано в услуге, потому что код оператора не совпадает с кодом оператора из базы оператора
                comment = String.format("Указанный номер телефона принадлежит оператору %s. Подайте заявление снова, указав правильного оператора связи.", pi.operatorName);
            } else {
                result = "3";
                comment = ""; // Передаем пустой комментарий в XSL. В этом случае элемент comment в ответе не появится.
            }
        } else {
            // Если телефон не найден, то формируем код отказа и комментарий к нему
            result = "4";
            comment = "Указанный номер телефона не принадлежит оператору. Обратитесь к вашему менеджеру.";
        }
        return;
    }

    public String getOrderId() {
        return orderId;
    }

    public String getLastName() {return lastName;}

    public String getFirstName() {return firstName;}

    public String getMiddleName() {
        return middleName;
    }

    public String getBirthDate() {
        return birthDate;
    }

    public String getDocType() {
        return docType;
    }

    public String getDocNumber() {return docNumber;}

    public String getDocDate() {
        return docDate;
    }

    public String getOperatorName() {
        return operatorName;
    }

    public String getCorpPhone() {
        return corpPhone;
    }

    public String getInn() {
        return inn;
    }

    public String getResult() {
        return result;
    }

    public String getClearPhone() {
        return clearPhone;
    }

    public String getComment() { return comment; }
}
