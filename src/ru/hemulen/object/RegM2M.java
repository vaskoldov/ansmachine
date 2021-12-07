package ru.hemulen.object;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.*;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.*;


public class RegM2M {
    private static Logger LOG = LoggerFactory.getLogger(ActivateNumber.class.getName());
    private String orderId;
    private String orderDate;
    private String inn;
    private String orgName;
    private String operatorName;
    private String operatorId;
    private String operatorIdAdd;
    private String routeNumber;
    private List<Device> devices;
    private String checkResult;
    private String comment;
    private Document application;   // DOM-объект с заявлением
    private Element root;       // Корень XML-документа
    private XPathFactory xPathFactory;
    private XPath xPath;

    public RegM2M(String xmlString, Properties props) {
        devices = new ArrayList<Device>();
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
        root = application.getDocumentElement();
        orderId = getString("orderId");
        orderDate = getString("orderDate");
        inn = getString("inn");
        orgName = getString("orgName");
        operatorName = getString("operatorName");
        operatorId = getString("operatorId");
        operatorIdAdd = getString("operatorIdAdd");
        routeNumber = getString("routeNumber");
        // Парсим раздел заявления со списком устройств (заполняется член класса devices)
        parseDevices();
    }

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
     * Метод проходит по всем элементам deviceData и формирует массив элементов devices
     * @return
     */
    private void parseDevices() {
        String query = "//*[local-name()='deviceData']";
        try {
            XPathExpression exp = xPath.compile(query);
            // Получаем массив элементов deviceData
            NodeList deviceNodes = (NodeList) exp.evaluate(root, XPathConstants.NODESET);
            Node deviceNode;
            for (int i=0; i < deviceNodes.getLength(); i++) {
                deviceNode = deviceNodes.item(i);
                Device device = new Device(deviceNode);
                devices.add(device);
            }
        } catch (XPathExpressionException e) {
            LOG.error(e.getMessage());
        }
    }

    public void check(String mnemonic, HashMap phoneInfoMap) {
        Boolean areAllNumbersCorrect = true;   // Признак, что все номера в списке правильные
        Boolean areAllNumbersIncorrect = true; // Признак, что все номера в списке неправильные
        for (ListIterator<Device> iter = devices.listIterator(); iter.hasNext();) {
            Device currDevice = iter.next();
            // Ищем номер в базе оператора
            if (phoneInfoMap.containsKey(currDevice.getClearPhoneNumber())) {
                // Номер найден в базе оператора
                // Теперь сверяем оператора из базы и оператора, которому направлено заявление
                // Для этого читаем запись базы оператора
                PhoneInfo pi = (PhoneInfo) phoneInfoMap.get(currDevice.getClearPhoneNumber());
                if (mnemonic.compareTo(pi.operatorMnemonic) != 0) {
                    // Номер обслуживается не тем оператором, которому направлено заявление
                    currDevice.setCheckResult("2"); // Не зарегистрировано
                    areAllNumbersCorrect = false; // Найден хотя бы один неправильный номер
                    currDevice.setComment(String.format("Номер принадлежит оператору %s.", pi.operatorName));
                } else {
                    currDevice.setCheckResult("1"); // Зарегистрировано
                    areAllNumbersIncorrect = false; // Найден хотя бы один правильный номер
                }
            } else {
                // Номер не найден в базе оператора
                currDevice.setCheckResult("2");
                currDevice.setComment("Номер не принадлежит оператору");
                areAllNumbersCorrect = false;
            }
        }
        if (!areAllNumbersCorrect && !areAllNumbersIncorrect) {
            checkResult = "67"; // Услуга частично оказана
            comment = "Оператор зарегистрировал не все указанные устройства";
        } else if (areAllNumbersCorrect) {
            checkResult = "3";  // Услуга оказана
            comment = "Оператор зарегистрировал все указанные устройства";
        } else if (areAllNumbersIncorrect) {
            checkResult = "4";  // Отказано в предоставлении услуги
            comment = "Ни одно из указанных устройств не зарегистрировано";
        }
    }

    /**
     * Метод формирует XML-ответ по параметрам и данным объекта regM2M и возвращает его в виде строки.
     * XSL в случае заявлений по ВС "Регистрация номеров M-to-M посредством ЕПГУ" не используется.
     * @param mnemonic - Мнемоника ИС УВ, от которой отправляется ответ (куда пришло заявление)
     * @param replyTo - ClientID сообщения с заявлением
     * @return - Строка, содержащая XML ответа
     */
    public String getResponse(String mnemonic, String replyTo) {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder;
        String responseXML;
        try {
            builder = factory.newDocumentBuilder();
            // Создаем пустой объект Document, в который будет собираться ответ
            Document doc = builder.newDocument();
            // Создаем корневой элемент
            Element rootElement = doc.createElementNS("urn://x-artefacts-smev-gov-ru/services/service-adapter/types", "ClientMessage");
            // Добавляем корневой элемент в документ
            doc.appendChild(rootElement);
            Element itSystem = doc.createElement("itSystem");
            itSystem.appendChild(doc.createTextNode(mnemonic));
            rootElement.appendChild(itSystem);
            Element responseMessage = doc.createElement("ResponseMessage");
            rootElement.appendChild(responseMessage);
            Element responseMetadata = doc.createElement("ResponseMetadata");
            responseMessage.appendChild(responseMetadata);
            Element clientId = doc.createElement("clientId");
            clientId.appendChild(doc.createTextNode(UUID.randomUUID().toString()));
            responseMetadata.appendChild(clientId);
            Element replyToClientId = doc.createElement("replyToClientId");
            replyToClientId.appendChild(doc.createTextNode(replyTo));
            responseMetadata.appendChild(replyToClientId);
            Element responseContent = doc.createElement("ResponseContent");
            responseMessage.appendChild(responseContent);
            Element content = doc.createElement("content");
            responseContent.appendChild(content);
            Element messagePrimaryContent = doc.createElement("MessagePrimaryContent");
            content.appendChild(messagePrimaryContent);
            // Собрали конверт адаптера и теперь добавляем бизнес-часть ответа
            Element responseRegM2M = doc.createElementNS("urn://gosuslugi/reg-m2m/1.0.1", "ResponseRegM2M");
            messagePrimaryContent.appendChild(responseRegM2M);
            Element changeOrderInfo = doc.createElement("changeOrderInfo");
            responseRegM2M.appendChild(changeOrderInfo);
            Element orderId = doc.createElement("orderId");
            orderId.appendChild(doc.createTextNode(this.orderId));
            changeOrderInfo.appendChild(orderId);
            Element statusCode = doc.createElement("statusCode");
            changeOrderInfo.appendChild(statusCode);
            Element techCode = doc.createElement("techCode");
            techCode.appendChild(doc.createTextNode(this.checkResult));
            statusCode.appendChild(techCode);
            if (this.comment != null && this.comment.length() > 0) {
                Element comment = doc.createElement("comment");
                comment.appendChild(doc.createTextNode(this.comment));
                changeOrderInfo.appendChild(comment);
            }
            Element deviceList = doc.createElement("deviceList");
            changeOrderInfo.appendChild(deviceList);
            for (ListIterator<Device> iter = devices.listIterator(); iter.hasNext();) {
                Device device = iter.next();
                Element deviceNode = doc.createElement("device");
                deviceList.appendChild(deviceNode);
                Element phoneNumber = doc.createElement("phoneNumber");
                phoneNumber.appendChild(doc.createTextNode(device.getPhoneNumber()));
                deviceNode.appendChild(phoneNumber);
                Element deviceNumber = doc.createElement("deviceNumber");
                deviceNumber.appendChild(doc.createTextNode(device.getDeviceNumber()));
                deviceNode.appendChild(deviceNumber);
                Element deviceType = doc.createElement("deviceType");
                deviceType.appendChild(doc.createTextNode(device.getDeviceType()));
                deviceNode.appendChild(deviceType);
                Element status = doc.createElement("status");
                status.appendChild(doc.createTextNode(device.getCheckResult()));
                deviceNode.appendChild(status);
                if (device.getComment().length() > 0) {
                    Element note = doc.createElement("note");
                    note.appendChild(doc.createTextNode(device.getComment()));
                    deviceNode.appendChild(note);
                }
            }
           responseXML = getStringFromDocument(doc);
        } catch (ParserConfigurationException | TransformerException e) {
            LOG.error(e.getMessage());
            return "";
        }
        return responseXML;
    }

    private String getStringFromDocument(Document doc) throws TransformerException {
        DOMSource domSource = new DOMSource(doc);
        StringWriter writer = new StringWriter();
        StreamResult result = new StreamResult(writer);
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();
        transformer.transform(domSource, result);
        return writer.toString();
    }

    public String getOrderId() {return orderId;}

    public String getOrderDate() {return orderDate;}

    public String getInn() {return inn;}

    public String getOrgName() {return orgName;}

    public String getOperatorName() {return operatorName;}

    public String getOperatorId() {return operatorId;}

    public String getOperatorIdAdd() {return operatorIdAdd;}

    public String getRouteNumber() {return routeNumber;}

    public List<Device> getDevices() {return devices;}

    public String getCheckResult() {return checkResult;}

    public String getComment() {return comment;}
}
