# maxoptra_test

Небольшое тестовое задание для компании Maxoptra.

## ЗАПУСК
Для запуска необходим Linux, Docker и Docker-compose. Все volumes и images создаются с префиксом ibelan_maxoptra_test, чтобы ни с чем не пересекаться.

* Запуск:
  * ./start.sh
* Остановка:
  * ./stop.sh
* Зачистка образов и томов (нужно раскомментировать зачистку образова для build-контейнера, чтобы зачистить и его тоже):
  * ./clean.sh

Сервис приложения будет доступен по адресу http://localhost:8080/app/gps.
Порт можно кастомизировать в файле ".env".

## СТЕК
* Java 11
* Spring Boot 2
* Hibernate
* Jetty
* Postgres 13
* Spatial4j
* Flyway
* Lombok
* Docker

## ВОПРОСЫ
Постановка вызывает ряд вопросов.

Самый спорный момент заключается во фразе:
> GPS позиции не всегда поступают упорядоченно по времени. Первыми могут прийти координаты от 12.07, а затем координаты от 12.06 и 12.05.

Было бы неплохо знать о характере этой неупорядоченности.
В случае если это перемешивание может происходить в диапазоне 2-3 минут, то можно записывать фактическое время прибытия/отбытия без дальнейших исправлений. Тогда в записях могут возникнуть небольшие неточности, допустимость которых является ещё одним вопросом.

В случае если вышеуказанные неточности недопустимы, или если данные могут приходить вообще абсолютно хаотично (как в файле с примером gps трекинга), то здесь не обойтись без исправления уже записанных прибытий/отбытий или хранения предварительных записей с откладыванием записи финальных значений, скажем, на сутки.

Также, вызывает вопрос фраза:
> Автомобили не всегда соблюдают порядок поставки. Иногда они могут его нарушить и выполнить выгрузки в точках 4,5,6 а затем в 1,2,3.

Кейс: автомобиль посетил локацию в час ночи, которую должен был посетить рано утром, а на следующее утро у него снова назначено посещение той же локации. К какому плановому посещению привязывать это фактическое посещение? Возможно, необходимо временное или локационное (стоянка на ночь) разграничение. Но так как и в расписании и в gps-трекинге указаны данные только на один день, то я просто пропущу этот момент.

Также непонятно может ли автомобиль проезжать в радиусе 300м возле некоторой локации, но не посещать её (до её фактического посещения/после её фактического посещения). На практике такое, думаю, более чем возможно, и поэтому учитывается в моей реализации.

## РЕАЛИЗАЦИЯ
Итого, с учётом набора допущений, реализован следующий алгоритм анализа трек-данных:
* запись трек-данных в таблице "gps" (она не зачищается, но по идее её нужно периодически зачищать)
* поиск ближайшей к автомобилю локации и проверка, что она ближе 300м
* вытягивание всех трек-данных автомобиля и их повторный анализ с записью и исправлением времени прибытия/отбытия:
  * все вытянутые данные выстраиваются в цепочку по времени
  * в этой цепочке находятся последовательностИ (мн.ч.!) gps позиций автомобиля в радиусе 300м от вышенайденной локации
  * выбирается последовательность с наибольшим кол-вом трек-данных (т.к. с наибольшей вероятностью именно в этом месте автомобиль остановился)
  * из этой последовательности берётся время первой gps-позиции и последней и записывается (или перезаписывается) как фактическое время прибытия и фактическое время отбытия

## ПРОВЕРКА
Для тестирования на тестовых данных добавлен скрипт sender.sh (требуется xmlstarlet)
