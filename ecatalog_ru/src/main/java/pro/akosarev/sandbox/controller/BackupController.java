package pro.akosarev.sandbox.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pro.akosarev.sandbox.service.PostgresBackupService;

@RestController
@RequestMapping("/api/admin/backup")
public class BackupController {

    private final PostgresBackupService backupService;

    public BackupController(PostgresBackupService backupService) {
        this.backupService = backupService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public String triggerBackup() {
        backupService.manualBackup();
        return "Backup process started";
    }
}
