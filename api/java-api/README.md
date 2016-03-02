### API для языка Java
Чтобы подключить библиотеку к вашему Java проекту, пропишите файле pom.xml вашего проекта следующую зависимость

```xml
<dependency>
  <groupId>com.aggregate</groupId>
  <artifactId>api</artifactId>
  <version>0.0.1</version>
  <scope>provided</scope>
</dependency>
```

#### Репозиторий
Не забудьте добавить в pom.xml ссылку на репозиторий

```xml
<repositories>
  <repository>
    <id>bintray-aggregate</id>
    <url>https://dl.bintray.com/uzyovoys/aggregate</url>
  </repository>
</repositories>
```
