public class App {
    public static void main(String[] args) {

        // ИСПЫТАНИЕ 1 с параметрами по исходному условию:
        // Философов 5, число обедов 3, длительность обеда ~500 мс,
        // (время размышления случайное не более 1 с)

        System.out.println("\nСТОЛОВАЯ 1 (Философов 5, число обедов 3, длительность обеда ~500 мс)\n");
        PhilosopherSettings.numOfLunches = 3;
        PhilosopherSettings.lunchDurationMs = 500L;
        PhilosopherSettings.maxThinkingDurationMs = 1000L;
        PhilosopherSettings.printMessages = true;

        var room = new DiningRoom(5);

        room.prepareForksAndTable();
        room.preparePhilosophers();
        room.startPhilosophers();

        // room.timeWait(5000);
        // room.stopPhilosophers();

        room.awaitPhilosophersEnd();
        room.printStatistics();
        System.out.println("Все Философы успешно \"завершились\"");

        // ИСПЫТАНИЕ 2:
        // Философов 8,
        // число обедов - ограничено общим временем испытания в 5 сек.,
        // длительность обеда и длительность размышления - минимальные (0)
        System.out.println("=".repeat(60));

        System.out.println(
                "\nСТОЛОВАЯ 2 (Философов 8, число обедов - ограничено временем 5 сек, длительность обеда мин.возм.))\n");
        PhilosopherSettings.numOfLunches = -1;
        PhilosopherSettings.lunchDurationMs = 0;
        PhilosopherSettings.maxThinkingDurationMs = 0;
        PhilosopherSettings.printMessages = false;

        room = new DiningRoom(8);

        room.prepareForksAndTable();
        room.preparePhilosophers();
        room.startPhilosophers();

        room.timeWait(5000);

        room.stopPhilosophers();
        room.awaitPhilosophersEnd();

        room.printStatistics();
        System.out.println("Все Философы успешно \"завершились\"");
    }
}
