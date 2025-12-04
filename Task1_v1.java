import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

public class Task1_v1 {
    public static void main(String[] args) throws ExecutionException, InterruptedException {
        FileReader1 fileReader = new FileReader1();
        TextRemove1 textRemove = new TextRemove1();
        ConsolePrint1 consolePrint = new ConsolePrint1();

        List<String> filePaths = Arrays.asList(
                "C:\\PW4\\text1.txt",
                "C:\\PW4\\text2.txt"
                //"C:\\PW4\\text3.txt"
                //"C:\\PW4\\text4.txt"
        );

        System.out.println("--Початок асинхронної роботи--\n");

        //зчитування інформації з файлів
        CompletableFuture<List<String>> readingTask = CompletableFuture.supplyAsync(() -> {
            long start = System.nanoTime();

            List<String> result = fileReader.readFiles(filePaths);
            consolePrint.printExecutionTime("Зчитування файлів", start);

            return result;
        });

        //вивід початкових даних
        CompletableFuture<Void> printOriginalTask = readingTask.thenAcceptAsync(sentences -> {
            long start = System.nanoTime();

            consolePrint.printList("Початковий текст", sentences, "Оригінальний рядок");
            consolePrint.printExecutionTime("Вивід початкових даних", start);
        });

        //видалення тексту
        CompletableFuture<List<String>> processingTask = readingTask.thenApplyAsync(sentences -> {
            long start = System.nanoTime();

            List<String> processed = textRemove.removeLetters(sentences);
            consolePrint.printExecutionTime("Обробка тексту", start);

            return processed;
        });

        //вивід результату
        CompletableFuture<Void> printResultTask = processingTask.thenAcceptAsync(processed -> {
            long start = System.nanoTime();

            consolePrint.printList("Результат видалення", processed, "Рядок після обробки");
            consolePrint.printExecutionTime("Вивід результату", start);
        });

        CompletableFuture.allOf(printOriginalTask, printResultTask).join();

        System.out.println("\n--Програма завершена--");
    }
}

//клас для читання файлів
class FileReader1 {
    public List<String> readFiles(List<String> paths) {
        List<String> sentences = new ArrayList<>();
        System.out.println("Thread [" + Thread.currentThread().getName() + "]: Починаю зчитування файлів...");

        for (String path : paths) {
            try {
                List<String> lines = Files.readAllLines(Paths.get(path));
                sentences.addAll(lines);
            } catch (IOException e) {
                System.err.println("Помилка! Не вдалося знайти або прочитати файл: " + path);
            }
        }
        return sentences;
    }
}

//клас для видалення тексту
class TextRemove1 {
    public List<String> removeLetters(List<String> input) {
        System.out.println("\nThread [" + Thread.currentThread().getName() + "]: Видалення літер...");

        return input.stream()
                .map(s -> s.replaceAll("[a-zA-Zа-яА-ЯіІїЇєЄ]", ""))
                .collect(Collectors.toList());
    }
}

//клас для виводу
class ConsolePrint1 {
    public void printList(String header, List<String> data, String linePrefix) {
        System.out.println("\nThread [" + Thread.currentThread().getName() + "]: " + header + ":");
        if (data.isEmpty()) {
            System.out.println("  (Список порожній)");
        } else {
            data.forEach(s -> System.out.println("  " + linePrefix + ": '" + s + "'"));
        }
    }
    public void printExecutionTime(String taskName, long startTime) {
        long end = System.nanoTime();
        System.out.printf("Час виконання '%s': %.3f мс%n", taskName, (end - startTime) / 1_000_000.0);
    }
}