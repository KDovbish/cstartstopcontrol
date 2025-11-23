package dk.startstopcontrol;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

/**
 * Класс, описывающий работу с командами менеджера томката
 */
class TomcatManager {

    private final String MANAGER_COMMAND_LIST = "/text/list";

    private String tomcatManagerUrl;   //  Формат: https://localhost:8443/manager
    private String user;
    private String password;

    private HttpURLConnection connection;


    /**
     * Конструктор при отсутствии базовой аутентификации
     * @param tomcatManagerUrl Урл менеджера томката. Пример: https://localhost:8443/manager
     */
    TomcatManager(String tomcatManagerUrl) {
        this.tomcatManagerUrl = tomcatManagerUrl;
    }

    /**
     * Конструктор при наличии базовой аутентификации
     * @param tomcatManagerUrl Урл менеджера томката. Пример: https://localhost:8443/manager
     * @param user Пользователь для базовой аутентификации (уже открытый, не кодированный) (м.б. null)
     * @param password Пароль для базовой аутентификации (уже открытый, не кодированный) (м.б. null)
     */
    TomcatManager(String tomcatManagerUrl, String user, String password) {
        this.tomcatManagerUrl = tomcatManagerUrl;
        this.user = user;
        this.password = password;
    }


    /**
     * Подключение к ресурсу с или без базовой авторизации
     * <p>
     * Данный метод недоступен за пределами данного класса, т.к. будет(т.к. должен) использоваться каждый раз при выполнении
     * той или иной команды
     * </p>
     * @param url Реальный урл подключения
     * @return Код ответа сервера
     * @throws IOException Проблемы ввода/вывода
     */
    private int connect(String url) throws IOException {
        connection = (HttpURLConnection) (new URL(url).openConnection());
        connection.setRequestMethod("GET");

        if (user != null) {
            //  Формируем строку user:password, которую будем далее кодировать в Base64
            String credentials = user + ":";
            if (password != null) {
                credentials = credentials + password;
            }

            //  Кодируем строку креденшиналов в Base64
            byte[] encodedCredentials = Base64.getEncoder().encode(  credentials.getBytes(StandardCharsets.UTF_8) );
            //  Добавляем в запрос заголовок для базовой авторизации
            connection.setRequestProperty("Authorization", "Basic " + new String(encodedCredentials));
        }

        //  --- Отладка ---
        if (ControlPanel.DEBUG) {
            System.out.println("connection.getResponseCode(): " + connection.getResponseCode());
        }

        //  Физическое подключение на ресурс и ответ от сервера
        return connection.getResponseCode();
    }


    /**
     * Выполнить команду менеджера list
     * <p>
     *     Команда list менеджера томката отдает результат приблизительно в таком виде:
     *     <pre>
     *         OK - Listed applications for virtual host [localhost]
     *         /:running:0:ROOT
     *         /RequestHeadersPrint:running:0:RequestHeadersPrint
     *         /hello-world:running:0:hello-world
     *         /examples:running:0:examples
     *         /host-manager:running:0:host-manager
     *         /manager:running:0:manager
     *         /docs:running:0:docs
     *     </pre>
     *     В результирующий список помещаются все строки этого вывода. Как видно, каждое из приложений - это отдельная строка
     *     в фиксированном формате. Как видно, описания приложений будут начинаться с 1-го элемента результирующего списка.
     * @return Список приложений, отдаваемый командой list менеджера томката.
     * @throws IOException Проблемы ввода/вывода
     */
    List<String> list() throws IOException {

        List<String> list = new ArrayList<>();

        if ( connect(tomcatManagerUrl + MANAGER_COMMAND_LIST) == HttpURLConnection.HTTP_OK ) {

            //  Получаем входной поток байтов тела ответа
            InputStream inputStream = connection.getInputStream();

            //  Создаем ридер символов, используя для этого объект-мост между байтовым и символьным потоком
            BufferedReader reader =  new BufferedReader(new InputStreamReader( inputStream, StandardCharsets.UTF_8 ));
            String s;
            while ( (s = reader.readLine()) != null ) {
                list.add(s);
            }

            //  Освобождаем все сетевые ресурсы связанные с текущим экземпляром HttpURLConnection
            //  Из документации HttpURLConnection:
            //  "... Calling the close() methods on the InputStream or OutputStream of an HttpURLConnection after
            //  a request may free network resources associated with this instance but has no effect on any shared
            //  persistent connection..."
            inputStream.close();
        }

        return list;
    }
}
