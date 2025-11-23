## Контроль зупинки томкатовських додатків, сповіщення про це на логстеш 

### Параметри командного рядка
```declarative
usage: java -jar cstartstopcontrol.jar [-d] [-l <host[:port]>] -m <manager url> [-p <password>] [-t <number of minutes>] [-u <user>]
Parameters:
-d,--debug  debug messages mode
-l,--logstash <host[:port]> logstash address
-m,--manager <manager url>  tomcat manager url
-p,--password <password>    Base64 encoded manager password
-t,--timeout <number of minutes>    timeout between pools(default 2 min)
-u,--user <user>    Base64 encoded manager user name
```


### Логіка модуля
Із заданим таймаутом опитується менеджер томкату: запитується список усіх додатків зі своїми статусами.
Йде відфільтрування зупинених додатків і відсилання повідомлень про такі додатки на логстеш.
  

### Приблизний приклад запуску модуля
```declarative
java -jar cstartstopcontrol.jar \
    -m https://tomcat.com.ua:8443/manager \
    -u JKhhk788687KJhk= \
    -p OOjkllbn543j= \
    -p logstash.com.ua:9005 \
    -t 1
```
