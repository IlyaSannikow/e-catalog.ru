package pro.akosarev.sandbox.service;

import io.minio.*;
import io.minio.messages.Item;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPOutputStream;

@Service
public class PostgresBackupService {

    private static final Logger logger = LoggerFactory.getLogger(PostgresBackupService.class);
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private final DataSource dataSource;
    private final MinioClient minioClient;
    private final String bucketName;

    @Autowired
    public PostgresBackupService(DataSource dataSource, MinioClient minioClient,
                                 @Value("${minio.bucket.name}") String bucketName) {
        this.dataSource = dataSource;
        this.minioClient = minioClient;
        this.bucketName = bucketName;
    }

    @Scheduled(cron = "0 1 * * * *") // Каждый день в 1:00 ночи
    public void scheduledBackup() {
        if (hasAdminRole()) {
            executor.submit(this::createAndUploadBackup);
        } else {
            logger.warn("Scheduled backup skipped - no ADMIN role present");
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    public void manualBackup() {
        executor.submit(this::createAndUploadBackup);
    }

    private boolean hasAdminRole() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }

    private void createAndUploadBackup() {
        try {
            String timestamp = LocalDateTime.now().format(DATE_FORMAT);
            String backupName = "backup_" + timestamp + ".sql.gz";
            String backupPath = "backups/" + backupName;

            byte[] backupData = createBackup();
            byte[] compressedData = compressData(backupData);

            uploadToMinio(backupPath, compressedData);
            logger.info("Backup successfully created and uploaded to MinIO: {}", backupPath);
        } catch (Exception e) {
            logger.error("Failed to create and upload backup", e);
        }
    }

    private byte[] createBackup() throws SQLException, IOException {
        try (Connection connection = dataSource.getConnection();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            // Устанавливаем параметры для долгих операций
            try (Statement setupStatement = connection.createStatement()) {
                setupStatement.execute("SET statement_timeout = 0");
                setupStatement.execute("SET lock_timeout = 0");
                setupStatement.execute("SET idle_in_transaction_session_timeout = 0");
            }

            // Получаем список всех таблиц
            try (Statement tableStatement = connection.createStatement();
                 ResultSet tables = tableStatement.executeQuery(
                         "SELECT table_name FROM information_schema.tables " +
                                 "WHERE table_schema = 'public' AND table_type = 'BASE TABLE'")) {

                while (tables.next()) {
                    String tableName = tables.getString(1);
                    dumpTable(connection, outputStream, tableName);
                }
            }

            return outputStream.toByteArray();
        }
    }

    private void dumpTable(Connection connection, ByteArrayOutputStream outputStream, String tableName)
            throws SQLException, IOException {

        // 1. Формируем DROP TABLE
        outputStream.write(("-- Table: " + tableName + "\n").getBytes());
        outputStream.write(("DROP TABLE IF EXISTS \"" + tableName + "\" CASCADE;\n\n").getBytes());

        // 2. Формируем CREATE TABLE
        outputStream.write(("CREATE TABLE \"" + tableName + "\" (\n").getBytes());
        dumpTableColumns(connection, outputStream, tableName);
        outputStream.write(");\n\n".getBytes());

        // 3. Добавляем индексы
        dumpTableIndexes(connection, outputStream, tableName);

        // 4. Добавляем данные
        dumpTableData(connection, outputStream, tableName);
    }

    private void dumpTableColumns(Connection connection, ByteArrayOutputStream outputStream, String tableName)
            throws SQLException, IOException {

        try (Statement columnStatement = connection.createStatement();
             ResultSet columns = columnStatement.executeQuery(
                     "SELECT column_name, data_type, character_maximum_length, " +
                             "is_nullable, column_default " +
                             "FROM information_schema.columns " +
                             "WHERE table_name = '" + tableName + "' " +
                             "ORDER BY ordinal_position")) {

            boolean firstColumn = true;
            while (columns.next()) {
                if (!firstColumn) {
                    outputStream.write(",\n".getBytes());
                }
                firstColumn = false;

                // Формируем определение колонки
                StringBuilder columnDef = new StringBuilder();
                columnDef.append("  \"").append(columns.getString("column_name")).append("\" ")
                        .append(columns.getString("data_type"));

                // Добавляем длину для строковых типов
                if (columns.getObject("character_maximum_length") != null) {
                    columnDef.append("(").append(columns.getInt("character_maximum_length")).append(")");
                }

                // Добавляем NULL/NOT NULL
                if ("NO".equals(columns.getString("is_nullable"))) {
                    columnDef.append(" NOT NULL");
                }

                // Добавляем значение по умолчанию
                if (columns.getObject("column_default") != null) {
                    columnDef.append(" DEFAULT ").append(columns.getString("column_default"));
                }

                outputStream.write(columnDef.toString().getBytes());
            }
        }
    }

    private void dumpTableIndexes(Connection connection, ByteArrayOutputStream outputStream, String tableName)
            throws SQLException, IOException {

        try (Statement indexStatement = connection.createStatement();
             ResultSet indexes = indexStatement.executeQuery(
                     "SELECT indexname, indexdef FROM pg_indexes " +
                             "WHERE tablename = '" + tableName + "'")) {

            while (indexes.next()) {
                outputStream.write((indexes.getString("indexdef") + ";\n").getBytes());
            }
            if (indexes.getRow() > 0) {
                outputStream.write("\n".getBytes());
            }
        }
    }

    private void dumpTableData(Connection connection, ByteArrayOutputStream outputStream, String tableName)
            throws SQLException, IOException {

        try (Statement dataStatement = connection.createStatement();
             ResultSet data = dataStatement.executeQuery("SELECT * FROM \"" + tableName + "\"")) {

            ResultSetMetaData metaData = data.getMetaData();
            int columnCount = metaData.getColumnCount();

            while (data.next()) {
                outputStream.write(("INSERT INTO \"" + tableName + "\" VALUES (").getBytes());
                for (int i = 1; i <= columnCount; i++) {
                    if (i > 1) outputStream.write(',');
                    Object value = data.getObject(i);
                    if (value == null) {
                        outputStream.write("NULL".getBytes());
                    } else {
                        outputStream.write('\'');
                        outputStream.write(data.getString(i).replace("'", "''").getBytes());
                        outputStream.write('\'');
                    }
                }
                outputStream.write(");\n".getBytes());
            }
            outputStream.write("\n".getBytes());
        }
    }

    private void dumpTableData(Statement statement, ByteArrayOutputStream outputStream, String tableName)
            throws SQLException, IOException {
        try (var rs = statement.executeQuery("SELECT * FROM \"" + tableName + "\"")) {
            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();

            while (rs.next()) {
                outputStream.write(("INSERT INTO \"" + tableName + "\" VALUES (").getBytes());
                for (int i = 1; i <= columnCount; i++) {
                    if (i > 1) outputStream.write(',');
                    Object value = rs.getObject(i);
                    if (value == null) {
                        outputStream.write("NULL".getBytes());
                    } else {
                        outputStream.write('\'');
                        outputStream.write(value.toString().replace("'", "''").getBytes());
                        outputStream.write('\'');
                    }
                }
                outputStream.write(");\n".getBytes());
            }
        }
        outputStream.write("\n".getBytes());
    }

    private byte[] compressData(byte[] data) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        try (GZIPOutputStream gzipOutputStream = new GZIPOutputStream(byteArrayOutputStream)) {
            gzipOutputStream.write(data);
        }
        return byteArrayOutputStream.toByteArray();
    }

    private void uploadToMinio(String objectName, byte[] data) throws Exception {
        try (InputStream inputStream = new ByteArrayInputStream(data)) {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .stream(inputStream, data.length, -1)
                            .contentType("application/gzip")
                            .build());
        }
    }

    @Scheduled(cron = "0 2 * * * *") // Через час после создания бэкапа
    public void cleanupOldBackups() {
        if (!hasAdminRole()) {
            logger.warn("Cleanup skipped - no ADMIN role present");
            return;
        }

        try {
            long cutoff = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(7); // 7 дней

            Iterable<Result<Item>> results = minioClient.listObjects(
                    ListObjectsArgs.builder()
                            .bucket(bucketName)
                            .prefix("backups/")
                            .build()
            );

            for (Result<Item> result : results) {
                try {
                    Item item = result.get();
                    if (item.lastModified().toInstant().toEpochMilli() < cutoff) {
                        minioClient.removeObject(
                                RemoveObjectArgs.builder()
                                        .bucket(bucketName)
                                        .object(item.objectName())
                                        .build()
                        );
                        logger.info("Deleted old backup: {}", item.objectName());
                    }
                } catch (Exception e) {
                    logger.error("Failed to process backup item", e);
                }
            }
        } catch (Exception e) {
            logger.error("Failed to cleanup old backups", e);
        }
    }
}
