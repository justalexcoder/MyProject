# Вариант решения классической проблемы "Обедающие философы"

### Классическое условие задачи:

Пять философов сидят за одним столом.
Каждый из них попеременно размышляет и обедает.
Для того чтобы обедать, философу нужны две вилки в руках &mdash; слева и справа.
Вилок всего пять штук. Пообедав, философ кладёт обе вилки обратно на стол,
после чего их может взять другой философ, повторяющий тот же цикл.
Когда философ не обедает, он размышляет.

_**Главная цель:**_ Не должно возникнуть общей блокировки, чтобы позволить философам размышлять и обедать, избежав голодной смерти.

### Дополнительные условия:
Философы не должны есть больше одного раза подряд.\
Каждый философ должен пообедать три раза.\
Каждый прием пищи длится 500 миллисекунд.\
После каждого приема пищи философ должен размышлять.

## Решение

Философы представлены экземплярами класса `class Philosopher`, расширяющего
класс `Thread`, т.е. как самостоятельные потоки.

Каждая Вилка является самостоятельным разделяемым ресурсом.
Вилки представлены экземплярами класса `class Fork`, расширяющего функционал
`ReentrantLock` для удобства обеспечения синхронизации доступа к ним из разных
потоков-Философов.

Количество Вилок на столе равно количеству Философов по условию.

Каждый экземпляр Вилки снабжён индивидуальным номером (идентификатором).\
Вилки раскладываются на столе против часовой стрелки в порядке возрастания их
номеров, таким образом, что вилка с наименьшим номером будет слева от первого
Философа, а с номером на единицу больше &mdash; справа от первого и слева от
второго Философа, и т.д.

`class Philosopher` переопределяет метод *run()*, реализующий жизненный цикл
потока,
добавляет методы *lunch()* и *think()*, соответствующие действиям
"обедать" и "размышлять",
инкапсулирует пару экземпляров `class Fork`, соответствующую паре вилок,
доступных Философу для использования во время обеда.

Порядок вызова методов на каждой итерации в жизненном цикле: *lunch()*,
затем *think()*, &mdash; выбран для соответствия условию &mdash; "после каждого
приема пищи философ должен размышлять".

Таким образом, можно утверждать следующее:

Состояние, при котором поток Философ имеет эксклюзивный доступ к своим левой и
правой вилкам, символизируя Философа, держащего обе вилки в руках, равнозначно
тому, что Философ *обедает*.

По окончании обеда Философ кладёт обе вилки на стол &mdash; освобождает оба
ресурса.

Состояние, при котором поток Философ не владеет ни одним ресурсом-Вилкой равнозначно
тому, что Философ *размышляет*.

Количество Философов, которые могут обедать одновременно &mdash; не более
половины от общего количества, т.е. при 5-и Философах одновременно могут обедать
не более 2-х.

`class PhilosopherSettings` &mdash; держатель общих для всех Философов
параметров:
* длительность обеда,
* число обедов,
* прочие параметры для отладки различных режимов.

`class DiningRoom` &mdash; контроллер, для удобного создания экземпляров Вилок,
Философов, их запуск, прерывание, ожидание и т.п., в клиентской части приложения
 &mdash; методе *main()*.

Чтобы обеспечить одновременный старт Философов используется общий экземпляр `CyclicBarrier`.

#### Проблема мёртвой блокировки:

В исходной архитектуре потенциально возможна ситуация мёртвой блокировки,
поскольку:
* существуют ресурсы, которыми могут владеть не более одного процесса (Вилки),
* существуют точки в которых один ресурс удерживается и другой ожидается для
взятия (сперва берётся одна Вилка, затем ожидается когда освободиться вторая),
* удерживаемые ресурсы не могут быть отобраны из вне,
* и, если не принять специальные меры, возможно циклическое бесконечное ожидание
(например, все Философы в системе одномоментно взяли по Вилке с левой стороны и
каждый из них стал ждать, когда освободится Вилка с его правой стороны, чего уже
 не может произойти без вмешательства,покуда сосед находится в аналогичном
 ожидании).

(Пример дампа стека потоков в ситуации мёртвой блокировки приведён в конце.)

В данном решении проблема мёртвой блокировки устранена нарушением последнего
условия её возникновения &mdash; просто заставляем одного из Философов
(последнего) сперва брать правую вилку и только затем левую.

Заметим следующее: у всех кроме последнего Философов Вилка слева
имеет номер меньший чем у Вилки справа. У последнего Философа наоборот &mdash;
Вилка справа меньше Вилки слева по номеру.

Так, в коде обеспечено, что каждый Философ первой будет пытаться взять Вилку с
меньшим номером из двух, а второй &mdash; Вилку с большим номером, что
автоматически влечёт нарушение условия мёртвой блокировки, она никогда не
возникнет.

#### Проблема равномерного распределения работы между потоками:

Для удовлетворения условия "Философы не должны есть больше одного раза подряд"
применяется следующая стратегия с использованием механизма ожидания/оповещения:
* Каждая Вилка наделена способностью сохранять сведения о том, кто из Философов
её использовал последним. Стратегия заключается в том, чтобы не дать
использовать Вилку дважды подряд одним и тем же Философом.
* Каждая Вилка, являясь производной от `ReentrantLock`, инкапсулирует связанный
с ней экземпляр `Condition`, позволяющий использовать механизм
ожидания/оповещения при работе с данной блокировкой.
* Так, когда Вилка берётся тем же Философом, который её использовал последним,
то эта блокировка временно освобождается, чтобы позволить другому Философу её
захватить и оповестить об этом первого Философа, который пока будет находится в
состоянии ожидания.
* Вилка, получив оповещение о том, что другой Философ воспользовался ею,
выводит из состояния ожидания первого Философа. Так обеспечивается взаимное
чередование пользования Вилкой.

Для статистики по каждому Философу (потоку) ведётся подсчёт фактически произошедших обедов.
Поскольку счётчик обедов инкрементируется исключительно в критической секции,
а его итоговое значение используется только по завершении жизненного цикла, т.е.
между изменениями поля в потоке и чтением из него после завершения потока итак
установлено отношение happens-before, то счётчик представлен обычным, т.е. не-volatile,
полем типа int.

### Пример вывода:

*Вариант 1* = Философов 5, число обедов 3, длительность обеда ~500&nbsp;мс,
длительность размышления случайное не более 1&nbsp;с.\
*Вариант 2 (только статистика как результат успешного завершения без мёртвой блокировки)* = Философов 8, число обедов ограничено только общим временем 5&nbsp;сек., длительность обеда и размышления минимально возможные (создание ситуации гонки за взятием блокировки).

![Пример](https://github.com/alexeycoder/illustrations/blob/main/java-jdk-hw5-dining-philosophers/dining-philosophers-example.png?raw=true)


<hr>

#### *Прил.*: Пример дампа стека потоков в ситуации мёртвой блокировки

```
Java stack information for the threads listed above:
===================================================
"Философ 1":
        at jdk.internal.misc.Unsafe.park(java.base@21/Native Method)
        - parking to wait for  <0x0000000718c2bf68> (a java.util.concurrent.locks.ReentrantLock$NonfairSync)
        at java.util.concurrent.locks.LockSupport.park(java.base@21/LockSupport.java:221)
        at java.util.concurrent.locks.AbstractQueuedSynchronizer.acquire(java.base@21/AbstractQueuedSynchronizer.java:754)
        at java.util.concurrent.locks.AbstractQueuedSynchronizer.acquire(java.base@21/AbstractQueuedSynchronizer.java:990)
        at java.util.concurrent.locks.ReentrantLock$Sync.lock(java.base@21/ReentrantLock.java:153)
        at java.util.concurrent.locks.ReentrantLock.lock(java.base@21/ReentrantLock.java:322)
        at Philosopher.takeFork(Philosopher.java:44)
        at Philosopher.run(Philosopher.java:27)
"Философ 2":
        at jdk.internal.misc.Unsafe.park(java.base@21/Native Method)
        - parking to wait for  <0x0000000718c2bfa0> (a java.util.concurrent.locks.ReentrantLock$NonfairSync)
        at java.util.concurrent.locks.LockSupport.park(java.base@21/LockSupport.java:221)
        at java.util.concurrent.locks.AbstractQueuedSynchronizer.acquire(java.base@21/AbstractQueuedSynchronizer.java:754)
        at java.util.concurrent.locks.AbstractQueuedSynchronizer.acquire(java.base@21/AbstractQueuedSynchronizer.java:990)
        at java.util.concurrent.locks.ReentrantLock$Sync.lock(java.base@21/ReentrantLock.java:153)
        at java.util.concurrent.locks.ReentrantLock.lock(java.base@21/ReentrantLock.java:322)
        at Philosopher.takeFork(Philosopher.java:44)
        at Philosopher.run(Philosopher.java:27)
"Философ 3":
        at jdk.internal.misc.Unsafe.park(java.base@21/Native Method)
        - parking to wait for  <0x0000000718c2bfd8> (a java.util.concurrent.locks.ReentrantLock$NonfairSync)
        at java.util.concurrent.locks.LockSupport.park(java.base@21/LockSupport.java:221)
        at java.util.concurrent.locks.AbstractQueuedSynchronizer.acquire(java.base@21/AbstractQueuedSynchronizer.java:754)
        at java.util.concurrent.locks.AbstractQueuedSynchronizer.acquire(java.base@21/AbstractQueuedSynchronizer.java:990)
        at java.util.concurrent.locks.ReentrantLock$Sync.lock(java.base@21/ReentrantLock.java:153)
        at java.util.concurrent.locks.ReentrantLock.lock(java.base@21/ReentrantLock.java:322)
        at Philosopher.takeFork(Philosopher.java:44)
        at Philosopher.run(Philosopher.java:27)
"Философ 4":
        at jdk.internal.misc.Unsafe.park(java.base@21/Native Method)
        - parking to wait for  <0x0000000718c2c010> (a java.util.concurrent.locks.ReentrantLock$NonfairSync)
        at java.util.concurrent.locks.LockSupport.park(java.base@21/LockSupport.java:221)
        at java.util.concurrent.locks.AbstractQueuedSynchronizer.acquire(java.base@21/AbstractQueuedSynchronizer.java:754)
        at java.util.concurrent.locks.AbstractQueuedSynchronizer.acquire(java.base@21/AbstractQueuedSynchronizer.java:990)
        at java.util.concurrent.locks.ReentrantLock$Sync.lock(java.base@21/ReentrantLock.java:153)
        at java.util.concurrent.locks.ReentrantLock.lock(java.base@21/ReentrantLock.java:322)
        at Philosopher.takeFork(Philosopher.java:44)
        at Philosopher.run(Philosopher.java:27)
"Философ 5":
        at jdk.internal.misc.Unsafe.park(java.base@21/Native Method)
        - parking to wait for  <0x0000000718c2bf30> (a java.util.concurrent.locks.ReentrantLock$NonfairSync)
        at java.util.concurrent.locks.LockSupport.park(java.base@21/LockSupport.java:221)
        at java.util.concurrent.locks.AbstractQueuedSynchronizer.acquire(java.base@21/AbstractQueuedSynchronizer.java:754)
        at java.util.concurrent.locks.AbstractQueuedSynchronizer.acquire(java.base@21/AbstractQueuedSynchronizer.java:990)
        at java.util.concurrent.locks.ReentrantLock$Sync.lock(java.base@21/ReentrantLock.java:153)
        at java.util.concurrent.locks.ReentrantLock.lock(java.base@21/ReentrantLock.java:322)
        at Philosopher.takeFork(Philosopher.java:44)
        at Philosopher.run(Philosopher.java:27)

Found 1 deadlock.
```