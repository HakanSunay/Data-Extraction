# ПРОЕКТ НА ТЕМА:

# TCP приложение за търсене на зависимост на поведение на потребители чрез LCMFreq алгоритъма
# Съдържание



1.         Описание на темата
2.         Имплементация
3.         Проектиране
4.         Реализация
5.         Инсталация
6.         Резултати
7.         Литература

# Описание на тема

Разглежданата тема изисква реализирането на клиент и сървър приложение чрез TCP протокола за търсене на зависимост на поведение на потребител. Сървърът анализира поведението на потребителите на база данните от файл пратен от клиента с помощта на откриване на шаблон на данните чрез реализирането на LCMFreq алгоритъм за извличането на данни. Накрая информацията се предоставя на клиента във вид удобен за визуализиране на зависимостите.

Самият LCMFreq алгоритъм приема за вход текстов файл, представляващ база данни от транзакции и число. Една транзакция се определя от елементите, които я изграждат.

Те трябва да са подредени във възх одящ ред във всяка транзакция и не трябва да се повтарят в нея. За транзакции приемаме отделните редове в нашия текстови файл. Алгоритъмът намира, кои елементи или групи от елементи се срещат най-често. Например, ако имаме елемент 1, който се повтаря на всеки ред в 1000-редов файл, то тогава в резултата на алгоритъма ще присъства ред, в който ще бъде посочено, че 1 участва 1000 пъти и така нататък за 2/3/4-елементните повторения. Параметърът minsup (Minimal Support) е процентовото съотношение между транзакциите във файла и броя на срещанията на всеки елемент, тоест ако имам файл с 1000 реда и параметър 0,5, то тогава алгоритъмът ще търси само такива елементи или групи от елементи, които се срещат \&gt;= 500 пъти. (За големи бази от данни се изискват много малки стойности на minsup за по-добри стойности).

За целите на проекта LCMFreq алгоритъмът ще приема като вход файла с логове, обаче преди това ще се наложи да го преобразува в удобна за работа форма, тоест да превърне всеки низ(String) в него в число(Integer) и да всеки ред да бъде възходящо сортиран.Резултатът ще представлява информация за това кои действия се извършват най-много от определените потребители и също така кои действия извършва определен потребител най-често.



# Решение на проблема

Приложението работи чрез клиент и сървър. Сървърът стартира new Thread().start() нишки за всеки свързал се клиент. Клиентът от своя страна след свързване подава от стандартния изход име на файл със структура: Time, Event context, Component, Event name, Description, Origin, IP address; и число и след това чака за да получи обратно от сървъра резултатният файл.

Нишката приема и запазва локално файла от клиента. Чрез Server класа файлът се форматира, така че да е подходящ за подаване на като вход на LCMFreq алгоритъма. Прилага LCMFreq алгоритъма върху файла и резултата се запазва в нов файл, който след това отново се обработва и се праща към клиента във вид, който е подходящ за визуализиране на зависимостите.

Клиентът приема и запазва файла локално, след което връзката се затваря.



# Проектиране

Проектът е изграден от 2 пакета:

- DataExtraction
- LCMFreq

# Реализация

# Server
 класът се състои от един main метод, който стартира сървъра и отговаря за слушането по дадения порт и приемането на конекции от клиенти. За всеки клиент, който се свързва към сървъра е стартирана new Thread().start() нишка. По този начин много клиенти могат да ползват сървъра едновременно.



**Server** класът е изграден от следните методи:

``         public void         startServer() ``

Стартира сървъра променяйки член-данната running към true.

``         public void         stopServer() ``

Стартира сървъра променяйки член-данната running към false.

``         public         void         listen()``

Съдържа цялата логика на класа. При всяка нова конекция пуска нова нишка, която да обработва клиента. Последователно извиква долу-изброените методи.

``         private void         receiveFile(DataInputStream clientData)``

Запазва локално файла, изпратен от клиента, като запазва оригиналното име. Също запазва и изпратеното число, представляващо minsup за алгоритъма.



``         private HashMap<String, Integer> preparedHashForAlgo(String fileName)``

На всеки IP адрес или събитие съпоставя число, което да му съответства, като са номерирани по ред на срещане във файла и сортира новите стойности на всеки ред, като ги запазва в нов файл &quot;TO\_THE\_ALGO\_&quot; + fileName Връща map, в който като ключ е самият IP адрес или събитие, а като стойност е неговият номер.

``         private void         runLCMFreqlgorithm()``

Прилага LCMFreq алгоритъма върху предварително създаденият файл с име &quot;TO\_THE\_ALGO\_&quot; + fileName, който съдържа информацията от файла, подаден от клиента, във вид, подходящ за алгоритъма. Резултатът се записва във файл с име &quot;TO\_THE\_MAP\_&quot; + fileName и извиква долу-описаната функция, за да преработи файла във вид, който е удобен за визуализиране и извиква sendResultFile(...), след като изчиства временните файлове.

``         private void         reverseMap(HashMap<String,Integer> h, String fileName)``

Създава файл с име &quot;READY\_TO\_BE\_SENT\_&quot; + fileName, в който записва в удобен формат резултатите от изпълнението на алгоритъма, изполвайки техника за backwards mapping, където стойността става ключ, а ключът стойност и добавя информация от вида, колко пъти се среща дадена комбинация.

``         void         sendResultFile(OutputStream s,         DataOutputStream dos,         String fileName) ``

Изпраща самия резултатен файл на клиента, който преди това е форматиран така, че да е подходящ за изобразяване на зависимостите.

``         void         cleanServerJunk(String fileName)``

Изтрива всички временно създадени файлове.

# Client
класът се състои от няколко метода, които подпомагат успешното реализиране на нужните функционалности на всеки потребител, който се нуждае от анализ на своите данни.



**Client** класът е изграден от:

``         private void         sendFile(File file,         float         minsup,         OutputStream s,         DataOutputStream dos)``

Изпраща файла, размера на файла и числото за minsup на сървъра.

``         void         receiveFile(DataInputStream clientData,         String fileName)``

Записва резултатния файл, който е пратен от сървъра, под име &quot;CLIENT\_RESULT\_&quot; + fileName.

``         private        File         enterValidFileName(Scanner sc)``

Метод, с който се осигурява, че въведеното от потребителя име на файл, е име на съществуващ текстов файл. Ако потребителят въведе 5 пъти грешен файл, то връзката на клиента към сървъра се разпада.

``        private         float         enterValidMinsup(Scanner sc)``

Метод, който гарантира, че въведеното число от потребителя е число между 0.0 и 1.0. Ако потребителя въведе 5 пъти неподходящо число, то за праг се взема числото 0.0001 и приложението продължава своята работа.

# Инсталация

Програмата се предоставя във zip формат и не изисква инсталация, единствено е необходимо да се разархивира.

За да се стартира програмата е необходима предварителна инсталация на Java JDK 8 или по-нова версия. Самото стартиране може да стане през конзолата чрез предварилтено позициониране в out/production/DataExtraction и изпълнение на командата:

``$ java DataExtraction.Server <порт>``

``$ java DataExtraction.Class <host> <port>``

или да се отвори проекта в IDE по желание.



# Резултати

Програмата може да работи с много клиенти едновременно. Клиентът подава на сървъра лог файл, който след прилагане на LCMFreq алгоритъма, бива върнат обратно на клиента.

Така реализираният алгоритъм приема за елемнти на транзакции IP адресите, имената на събитията, които са били изпълнени от потребител с даденото IP, като се позиционират на един и същ ред.

След прилагането на алгоритъма получава сведения, какви елементи или групи от елементи се срещат най-често, което от своя страна може да ни послужи, да разберем какво правят студентите по време на изпит по Мрежово програмиране. Може да видим колко пъти е заредена страницата в мудъл на курса по време на изпита, за да си направим статистика колко често студентите преписват, но ествестно, тъй като сме добри хора такива лоши деяние са чужди за нас.


# Литература

- [http://www.philippe-fournier-viger.com/spmf/LCMFreq.php](http://www.philippe-fournier-viger.com/spmf/LCMFreq.php)         информация и реализация нa LCMFreq алгоритъма



- [https://learn.fmi.uni-sofia.bg/course/view.php?id=4649](https://learn.fmi.uni-sofia.bg/course/view.php?id=4649)
страница на курса по Мрежово програмиране, ФМИ

