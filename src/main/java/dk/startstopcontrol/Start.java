package dk.startstopcontrol;

import org.apache.commons.cli.*;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class Start {

    public static void main(String[] args) throws IOException, InterruptedException {

        Options options = new Options()
                .addOption(Option.builder("m").longOpt("manager").hasArg(true).required(true).desc("tomcat manager url").argName("manager url").build())
                .addOption(Option.builder("u").longOpt("user").hasArg(true).required(false).desc("Base64 encoded manager user name").argName("user").build())
                .addOption(Option.builder("p").longOpt("password").hasArg(true).required(false).desc("Base64 encoded manager password").argName("password").build())
                .addOption(Option.builder("l").longOpt("logstash").hasArg(true).required(false).desc("logstash address").argName("host[:port]").build())
                .addOption(Option.builder("t").longOpt("timeout").hasArg(true).required(false).desc("timeout between pools(default 2 min)").argName("number of minutes").build())
                .addOption(Option.builder("d").longOpt("debug").hasArg(false).required(false).desc("debug messages mode").build());

        try {
            //  Разбор параметров коммандной строки.
            //  В случае каких-либо проблем, будет сгенерировано исключение.
            CommandLine commandLine = new DefaultParser().parse(options, args);

            //  Включение отладочных сообщений на консоль, если пользователем этого потребовал
            if (commandLine.hasOption("debug")) {ControlPanel.DEBUG = true;}

            //  Записываем полученный от пользователя адрес логстеша в системную переменную, которую "видит" logback.xml
            if (commandLine.hasOption("logstash")) {
                System.setProperty("LOGSTASHADDR", commandLine.getOptionValue("logstash"));
            }

            //  --- Отладка ---
            if (ControlPanel.DEBUG) {
                System.out.println("LOGSTASHADDR: " + System.getProperty("LOGSTASHADDR"));
            }

            //  Основная логика программы: проверка не остановлено ли какое-либо приложение и отсылка об этом факте сообщения на логстеш
            MainLogic mainLogic = new MainLogic(commandLine);
            for(;;) {
                mainLogic.run();

                //  --- Отладка ---
                if (ControlPanel.DEBUG) {System.out.println(commandLine.getOptionValue("timeout", ControlPanel.TIMEOUT_DEFAULT) + " min delay is starting...");}
                //  Пауза между опросами менеджера
                TimeUnit.MINUTES.sleep( Integer.valueOf(commandLine.getOptionValue("timeout", ControlPanel.TIMEOUT_DEFAULT)) );
            }

        } catch (ParseException pe) {
            new HelpFormatter().printHelp("java -jar cstartstopcontrol.jar", "Parameters:", options, "", true);
        }

    }
}
