package ru.hemulen.object;

import org.w3c.dom.Node;

import javax.xml.xpath.*;

public class Device {
    private String orgAddress;
    private String deviceAddress;
    private String phoneNumber;
    private String clearPhoneNumber;
    private String deviceNumber;
    private String deviceType;
    private String deviceName;
    private String mfrName;
    private XPath xPath;
    private String checkResult; // Результат проверки конкретного устройства
    private String comment;     // Комментарий к отказу

    public Device(Node deviceNode) {
        xPath = XPathFactory.newInstance().newXPath();
        String query;
        XPathExpression exp;
        try {
            query = "*[local-name()='orgAddress']";
            exp = xPath.compile(query);
            orgAddress = (String) exp.evaluate(deviceNode, XPathConstants.STRING);
            query = "*[local-name()='deviceAddress']";
            exp = xPath.compile(query);
            deviceAddress = (String) exp.evaluate(deviceNode, XPathConstants.STRING);
            query = "*[local-name()='phoneNumber']";
            exp = xPath.compile(query);
            phoneNumber = (String) exp.evaluate(deviceNode, XPathConstants.STRING);
            query = "*[local-name()='deviceNumber']";
            exp = xPath.compile(query);
            deviceNumber = (String) exp.evaluate(deviceNode, XPathConstants.STRING);
            query = "*[local-name()='deviceType']";
            exp = xPath.compile(query);
            deviceType = (String) exp.evaluate(deviceNode, XPathConstants.STRING);
            query = "*[local-name()='deviceName']";
            exp = xPath.compile(query);
            deviceName = (String) exp.evaluate(deviceNode, XPathConstants.STRING);
            query = "*[local-name()='mfrName']";
            exp = xPath.compile(query);
            mfrName = (String) exp.evaluate(deviceNode, XPathConstants.STRING);
            clearPhoneNumber = phoneNumber.replaceAll("[^0-9]", "");
            if (clearPhoneNumber.length() >= 10) {
                // Если передан нормальный номер, то обрезаем справа 10 знаков, чтобы отсечь 7 и 8 в начале
                clearPhoneNumber = clearPhoneNumber.substring(clearPhoneNumber.length()-10);
            }   // Если передан короткий номер, то оставляем как есть - все равно он не пройдет проверку
        } catch (XPathExpressionException e) {
            e.printStackTrace();
        }
    }

    public String getOrgAddress() {
        return orgAddress;
    }

    public String getDeviceAddress() {
        return deviceAddress;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getClearPhoneNumber() {
        return clearPhoneNumber;
    }

    public String getDeviceNumber() {
        return deviceNumber;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public String getMfrName() {
        return mfrName;
    }

    public String getCheckResult() {
        return checkResult;
    }

    public String getComment() {
        if (comment != null) {
            return comment;
        }
        return "";
    }

    public void setCheckResult(String checkResult) {
        this.checkResult = checkResult;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
