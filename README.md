# MovieSearcher - приложение по поиску фильмов
Данное приложение помогает узнавать какие фильмы сейчас популярны, находятся в прокате или скоро будут в нём.
Также пользователь может добавлять карточки фильмов в избранное или создавать напоминания о просмотре на определённую дату.

### Скачать приложение
[![](https://github.com/Sanektys/MovieSearcher/assets/27242252/ce94e06b-a503-452c-bc9e-169f184c54fc)](https://1drv.ms/u/s!AtyN7egB4WeupES2NgRHFFAEXEG-?e=lPsqtI "Скачать APK приложения через OneDrive")

> [!NOTE]
> **MovieSearcher использует API The Movie Database (TMDB).** Этот сервис заблокирован на территории РФ и Беларуси. Поэтому для того, чтобы пользоваться приложением из этих стран, необходим VPN умеющий подменять DNS

## Ключевые особенности приложения
### Поиск фильмов и работа в офлайне
MovieSearcher позволяет производить поиск фильмов на серверах TMDB и отображать списки фильмов по категориям: "*Популярное*", "*С высоким рейтингом*", "*Скоро в прокате*" и "*В прокате*".  
Если пользователь уже ранее просматривал вышеперечисленные списки, но вдруг пропало интернет-соединение, то будут показаны закэшированные списки (т.е. последние загруженные ещё при наличии интернета).

Для бóльшего удобства пользователя в приложении можно:  
- переключать языки (русский/английский);
- менять тему (светлая/тёмная/как в системе);
- включать/отключать некоторые анимации (и автоматически их отключать при низком заряде батареи).

https://github.com/Sanektys/MovieSearcher/assets/27242252/da400d7d-691c-4b20-81d4-c4134312f6c2

### Сохранение фильмов в избранном и создание напоминаний о просмотре
В MovieSearcher можно добавить фильм в избранное, сохранив, таким образом, его карточку в памяти устройства. Список таких фильмов можно найти на экране "*Избранное*"  
Дополнительно есть список "*Смотреть позже*" на одноимённом экране, в который можно добавить фильм, создав для него напоминание о просмотре. Дату и время напоминания устанавливает пользователь.
По достижению даты и времени, фильм будет удалён из данного списка и появится уведомление(нотификация) о том, что пользователь хотел посмотреть этот фильм.

https://github.com/Sanektys/MovieSearcher/assets/27242252/0f32ef87-87f7-47f0-9038-4cafbbe712ae

## О проекте
Представленное приложение было реализовано как сквозной проект в рамках курса по Android разработке от компании **SkillFactory**.

Всю хронологию разработки можно проследить по [Pull-request](https://github.com/Sanektys/MovieSearcher/pulls?q=is%3Apr+is%3Aclosed)'ам, в каждом из которых есть сводка 
по проделанной работе с демонстрацией результатов.
