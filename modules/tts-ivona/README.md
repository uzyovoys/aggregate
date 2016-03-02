### Синтезатор речи Ivona
С помощью данного модуля можно синтезировать речь с помощью онлайн сервиса Ivona.

[Скачать версию 1.0.0](https://bintray.com/artifact/download/uzyovoys/aggregate/com/aggregate/tts-ivona/1.0.0/tts-ivona-1.0.0.jar)

#### Установка
Для работы необходимо получить персональные ключи доступа к сервису Ivona Speech Cloud.
50000 запросов в месяц предоставляются *бесплатно*. Этого вполне достаточно для каждодневного использвоания.

Создайте аккаунт на сайте (http://ivona.com) и получите ключи. Подробнее об этой процедуре [можно прочитать здесь](http://b2b.support.ivona.com/articles/en_US/FAQ/sign-into-Speech-Cloud/?l=en_US&fs=RelatedArticle).

В вашем личном кабинете сгенерируйте ключи "Access Key" и "Secret Key".
Их нужно будет указать в файле _tts-ivona.json_ в директории _conf_ перед установкой модуля.

Пример файла конфигурации:

```javascript
{
  "accessKey" : "ваш Access Key",
  "secretKey" : "ваш Secret Key"
}
```

#### Голос
Вы можете указать, какой голос должен использовать синтезатор. Для этого укажите в файле _tts-ivona.json_ параметры _voice_ и _gender_. Например:

```javascript
{
  "accessKey" : "ваш Access Key",
  "secretKey" : "ваш Secret Key",
  "voice" : "Maxim",
  "gender" : "Male"
}
```

На данный момент поддерживаются два голоса - Maxim и Tatyana. _По умолчанию используется голос Tatyana_.
