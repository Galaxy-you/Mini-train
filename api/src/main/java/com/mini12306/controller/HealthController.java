package com.mini12306.controller;

import com.mini12306.model.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

/**
 * Health Check Controller
 * Provides endpoints for system health monitoring
 */
@RestController
@RequestMapping("/api/health")
public class HealthController {
    
    @Autowired(required = false)
    private DataSource dataSource;
    
    /**
     * Basic health check endpoint
     * Returns OK if the application is running
     */
    @GetMapping
    public Result<Map<String, Object>> health() {
        Map<String, Object> healthInfo = new HashMap<>();
        healthInfo.put("status", "UP");
        healthInfo.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        healthInfo.put("application", "Mini12306 API");
        healthInfo.put("version", "1.0.0");
        
        return Result.success(healthInfo);
    }
    
    /**
     * Detailed health check with database connectivity
     * Returns detailed status of application components
     */
    @GetMapping("/detailed")
    public Result<Map<String, Object>> detailedHealth() {
        Map<String, Object> healthInfo = new HashMap<>();
        healthInfo.put("application", "Mini12306 API");
        healthInfo.put("version", "1.0.0");
        healthInfo.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        
        // Check database connectivity
        Map<String, String> databaseStatus = new HashMap<>();
        try {
            if (dataSource != null) {
                try (Connection conn = dataSource.getConnection()) {
                    if (conn.isValid(5)) {
                        databaseStatus.put("status", "UP");
                        databaseStatus.put("database", conn.getMetaData().getDatabaseProductName());
                        databaseStatus.put("version", conn.getMetaData().getDatabaseProductVersion());
                    } else {
                        databaseStatus.put("status", "DOWN");
                        databaseStatus.put("message", "Database connection is not valid");
                    }
                }
            } else {
                databaseStatus.put("status", "UNKNOWN");
                databaseStatus.put("message", "DataSource not available");
            }
        } catch (Exception e) {
            databaseStatus.put("status", "DOWN");
            databaseStatus.put("error", e.getMessage());
        }
        healthInfo.put("database", databaseStatus);
        
        // Check JVM memory
        Map<String, Object> memoryInfo = new HashMap<>();
        Runtime runtime = Runtime.getRuntime();
        long maxMemory = runtime.maxMemory();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;
        
        memoryInfo.put("max", formatBytes(maxMemory));
        memoryInfo.put("total", formatBytes(totalMemory));
        memoryInfo.put("used", formatBytes(usedMemory));
        memoryInfo.put("free", formatBytes(freeMemory));
        memoryInfo.put("usagePercentage", String.format("%.2f%%", (usedMemory * 100.0) / totalMemory));
        healthInfo.put("memory", memoryInfo);
        
        // Overall status
        String overallStatus = "UP";
        if ("DOWN".equals(databaseStatus.get("status"))) {
            overallStatus = "DOWN";
        }
        healthInfo.put("status", overallStatus);
        
        return Result.success(healthInfo);
    }
    
    /**
     * Format bytes to human-readable format
     */
    private String formatBytes(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        }
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        char pre = "KMGTPE".charAt(exp - 1);
        return String.format("%.2f %sB", bytes / Math.pow(1024, exp), pre);
    }
    
    /**
     * Ping endpoint for load balancers
     */
    @GetMapping("/ping")
    public String ping() {
        return "pong";
    }
}
