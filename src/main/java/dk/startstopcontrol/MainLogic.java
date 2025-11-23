package dk.startstopcontrol;

import org.apache.commons.cli.CommandLine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ConnectException;
import java.util.Base64;
import java.util.List;

class MainLogic {

    private static final Logger logger = LoggerFactory.getLogger(MainLogic.class);
    private CommandLine commandLine;

    MainLogic(CommandLine commandLine) {
        this.commandLine = commandLine;
    }

    void run() throws IOException {

        //  Урл менеджера будет использоваться несколько раз ниже
        String managerUrl = commandLine.getOptionValue("manager");

        if (ControlPanel.DEBUG) {
            System.out.println("user: " + (commandLine.getOptionValue("user") != null ? (new String(Base64.getDecoder().decode(commandLine.getOptionValue("user")))).replace('\r', ' ').trim() : null));
            System.out.println("password: " + (commandLine.getOptionValue("password") != null ? (new String(Base64.getDecoder().decode(commandLine.getOptionValue("password")))).replace('\r', ' ').trim() : null));
        }

        //  Объект, реализующий операции с менеджером томката.
        //  Помним, что данный объект работает с открытыми(не шифрованными) креденшиналами.
        //  Помним, что в целом, данный модуль подразумевает, что пользователь в командной строке задает креденшиналы закодированные в Base64.
        //  Поэтому, если все же креденшиналы пользователем заданы(т.е они не null), то производим декодирование их перед передачей в объект.
        //
        //  По какой-то странной причине, декодирование base64 отдает исходную строку, но в конце этой строки добавлен байт 0x0A.
        //  Как я его удаляю, видно ниже.
        TomcatManager tomcatManager = new TomcatManager(
                managerUrl,
                commandLine.getOptionValue("user") != null ?
                        (new String(Base64.getDecoder().decode(commandLine.getOptionValue("user")))).replace('\r', ' ').trim() :
                        null ,
                commandLine.getOptionValue("password") != null ?
                        (new String(Base64.getDecoder().decode(commandLine.getOptionValue("password")))).replace('\r', ' ').trim() :
                        null
                );

        //  Для хранения списка приложений, полученных от менеджера
        List<String> appList;

        //  Подключаемся к менеджеру, чтобы получить список приложений...
        //  В случае неуспеха, отсылаем сообщение об этом на логстеш и завершаем метод.
        try {
            appList = tomcatManager.list();
        } catch (ConnectException e) {
            logger.info("url::=" + managerUrl + " action::=notconnected");
            return;
        }

        //  --- Отладка ---
        if (ControlPanel.DEBUG) {
            appList.forEach(System.out::println);
        }

        //  Url томката будет использовать в цикле ниже
        String tomcatUrl = managerUrl.substring(0, managerUrl.lastIndexOf("manager"));
        String[] ss;
        for (int i = 1; i <= appList.size() - 1; i++) {
            ss = appList.get(i).split(":");
            //  В случае если приложение остановлено, отправляем сообщение об этом на логстеш
            if (ss[1].contains("stopped")) {

                logger.info("url::=" + tomcatUrl + ss[3] + " action::=stopped");

                //  url успешно передается в поле Endpoint на ELK
                //logger.info("url::=https://kfredhapp01.sense.bank.int/" + ss[3] + " action::stopped");

                //  url успешно передается в поле Endpoint на ELK
                //logger.info("WSCallout.send msgId::=61493feae25648db url::=https://kfredhapp01.sense.bank.int/" + ss[3] + " action::stopped");

                //  url успешно передается в поле Endpoint на ELK
                //logger.info("WSCallout.send msgId::=61493feae25648db url::=https://ds-csense-reference-data.apps.core.google1.sense.bank.int/services/Dictionary2PT.Dictionary2PTHttpSoap11Endpoint action::stopped");

                // logger.info("WSCallout.send msgId::=61493feae25648db url::https://" + ss[3] + " action::stopped");
                // logger.error(ss[3] + ":stopped");
            }
        }
    }

}
