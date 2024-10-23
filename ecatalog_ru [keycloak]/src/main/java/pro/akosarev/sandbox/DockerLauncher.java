package pro.akosarev.sandbox;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class DockerLauncher {
    public static void main(String[] args) {
        try {
            // Запуск docker-compose up
            ProcessBuilder processBuilder = new ProcessBuilder("docker-compose", "-f",
                    "src\\main\\resources\\docker-ecatalog_ru\\docker-compose.yml",
                    "up", "--build");
            processBuilder.directory(new java.io.File("."));
            processBuilder.redirectErrorStream(true); // Объединяем стандартный и поток ошибок

            Process process = processBuilder.start();
            logProcessOutput(process);

            // Ждем завершения работы процесса
            int exitCode = process.waitFor();

            if (exitCode != 0) {
                System.out.println("Ошибка при создании или запуске контейнеров. Код ошибки: " + exitCode);
            }

        } catch (IOException | InterruptedException e) {
            System.out.println("Произошла ошибка: " + e.getMessage());
        }
    }

    public static void stopContainers() {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder("docker-compose", "-f",
                    "src\\main\\resources\\docker-ecatalog_ru\\docker-compose.yml",
                    "stop"); // Изменено на 'stop'
            processBuilder.directory(new java.io.File("."));
            processBuilder.redirectErrorStream(true);

            Process process = processBuilder.start();
            logProcessOutput(process);

            // Ждем завершения работы процесса
            int exitCode = process.waitFor();

            if (exitCode != 0) {
                System.out.println("Ошибка при остановке контейнеров. Код ошибки: " + exitCode);
            }

        } catch (IOException | InterruptedException e) {
            System.out.println("Произошла ошибка: " + e.getMessage());
        }
    }

    private static void logProcessOutput(Process process) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line;

        while ((line = reader.readLine()) != null) {
            // Проверка для вывода информации о создании контейнеров
            if (line.contains("Creating") || line.contains("Volumes") || line.contains("Starting")) {
                System.out.println(line);
            }
        }
    }
}
