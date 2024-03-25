## Как обновить библиотеку

* Склонировать проект на локальную машину
* Скопировать новый файл `openapi.yml` в директорию `specs`
* Запустить команду `./gradlew generateApi`
* Проверить, что сгенерированный код не содержит ошибок, запустив `./gradlew build`
* Запушить изменения в Gitlab и слить в ветку `master`

## Документация

Автоматически сгенерированная документация находится в [директории readme](readme/README.md).