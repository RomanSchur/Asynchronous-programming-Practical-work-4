import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;

public class Task2 {
    public static void main(String[] args) throws ExecutionException, InterruptedException {

        NumberGenerator generator = new NumberGenerator();
        MathCalculator calculator = new MathCalculator();
        MathPrinter printer = new MathPrinter();

        System.out.println("--Початок роботи--\n");

        //генерація чисел
        CompletableFuture<Void> mathChain = CompletableFuture.supplyAsync(() -> {
                    long start = System.nanoTime();
                    List<Double> numbers = generator.generate(20);

                    printer.printTime("Генерація масиву", start);
                    return numbers;
                })

                //вивід початкового масиву
                .thenApplyAsync(numbers -> {
                    long start = System.nanoTime();
                    printer.printSequence("Початкова послідовність", numbers);
                    printer.printTime("Вивід масиву", start);
                    return numbers;
                })

                //математичні обчислення
                .thenApplyAsync(numbers -> {
                    long start = System.nanoTime();
                    Double result = calculator.calculateSumOfProducts(numbers);
                    printer.printTime("Обчислення формули", start);
                    return result;
                })

                //вивід результату
                .thenAcceptAsync(result -> {
                    long start = System.nanoTime();
                    printer.printResult("РЕЗУЛЬТАТ ОБЧИСЛЕННЯ", result);
                    printer.printTime("Вивід результату", start);
                });
        mathChain.join();
        System.out.println("\n--Робота завершена--");
    }
}

//клас для генерації чисел
class NumberGenerator {
    public List<Double> generate(int count) {
        System.out.println("Thread [" + Thread.currentThread().getName() + "]: Генерація 20 чисел...");
        Random random = new Random();
        return DoubleStream.generate(() -> random.nextDouble() * 10)
                .limit(count)
                .boxed()
                .collect(Collectors.toList());
    }
}

//клас для обчислень
class MathCalculator {
    public Double calculateSumOfProducts(List<Double> numbers) {
        System.out.println("\nThread [" + Thread.currentThread().getName() + "]: Обчислення суми добутків...");
        double sum = 0;
        for (int i = 0; i < numbers.size() - 1; i++) {
            sum += numbers.get(i) * numbers.get(i + 1);
        }
        return sum;
    }
}

//клас для виводу
class MathPrinter {
    public void printSequence(String header, List<Double> numbers) {
        System.out.println("\nThread [" + Thread.currentThread().getName() + "]: " + header + ":");
        System.out.print("  [ ");
        for (int i = 0; i < numbers.size(); i++) {
            System.out.printf("%.2f", numbers.get(i));
            if (i < numbers.size() - 1) System.out.print(", ");
        }
        System.out.println(" ]");
    }
    public void printResult(String header, Double value) {
        System.out.println("\nThread [" + Thread.currentThread().getName() + "]: " + header + ":");
        System.out.printf("  Result = %.4f%n", value);
    }
    public void printTime(String taskName, long startTime) {
        long end = System.nanoTime();
        System.out.printf("Час виконання '%s': %.3f мс%n", taskName, (end - startTime) / 1_000_000.0);
    }
}