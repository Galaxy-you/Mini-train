package com.mini12306.controller;

import com.mini12306.model.Result;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Health Controller Unit Tests
 */
public class HealthControllerTest {
    
    @Mock
    private DataSource dataSource;
    
    @InjectMocks
    private HealthController healthController;
    
    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }
    
    @Test
    @DisplayName("Test basic health check returns UP status")
    public void testHealthCheck() {
        // Execute
        Result<Map<String, Object>> result = healthController.health();
        
        // Verify
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertNotNull(result.getData());
        assertEquals("UP", result.getData().get("status"));
        assertEquals("Mini12306 API", result.getData().get("application"));
        assertEquals("1.0.0", result.getData().get("version"));
        assertNotNull(result.getData().get("timestamp"));
    }
    
    @Test
    @DisplayName("Test detailed health check with successful database connection")
    public void testDetailedHealthWithDatabaseUp() throws Exception {
        // Setup
        Connection mockConnection = mock(Connection.class);
        DatabaseMetaData mockMetadata = mock(DatabaseMetaData.class);
        
        when(dataSource.getConnection()).thenReturn(mockConnection);
        when(mockConnection.isValid(5)).thenReturn(true);
        when(mockConnection.getMetaData()).thenReturn(mockMetadata);
        when(mockMetadata.getDatabaseProductName()).thenReturn("MySQL");
        when(mockMetadata.getDatabaseProductVersion()).thenReturn("8.0.33");
        
        // Execute
        Result<Map<String, Object>> result = healthController.detailedHealth();
        
        // Verify
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertNotNull(result.getData());
        assertEquals("UP", result.getData().get("status"));
        
        @SuppressWarnings("unchecked")
        Map<String, String> databaseStatus = (Map<String, String>) result.getData().get("database");
        assertNotNull(databaseStatus);
        assertEquals("UP", databaseStatus.get("status"));
        assertEquals("MySQL", databaseStatus.get("database"));
        assertEquals("8.0.33", databaseStatus.get("version"));
        
        @SuppressWarnings("unchecked")
        Map<String, Object> memoryInfo = (Map<String, Object>) result.getData().get("memory");
        assertNotNull(memoryInfo);
        assertNotNull(memoryInfo.get("max"));
        assertNotNull(memoryInfo.get("total"));
        assertNotNull(memoryInfo.get("used"));
        assertNotNull(memoryInfo.get("free"));
        assertNotNull(memoryInfo.get("usagePercentage"));
        
        verify(mockConnection, times(1)).close();
    }
    
    @Test
    @DisplayName("Test detailed health check with database connection failure")
    public void testDetailedHealthWithDatabaseDown() throws Exception {
        // Setup
        when(dataSource.getConnection()).thenThrow(new RuntimeException("Connection failed"));
        
        // Execute
        Result<Map<String, Object>> result = healthController.detailedHealth();
        
        // Verify
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertNotNull(result.getData());
        assertEquals("DOWN", result.getData().get("status"));
        
        @SuppressWarnings("unchecked")
        Map<String, String> databaseStatus = (Map<String, String>) result.getData().get("database");
        assertNotNull(databaseStatus);
        assertEquals("DOWN", databaseStatus.get("status"));
        assertNotNull(databaseStatus.get("error"));
    }
    
    @Test
    @DisplayName("Test detailed health check with invalid database connection")
    public void testDetailedHealthWithInvalidConnection() throws Exception {
        // Setup
        Connection mockConnection = mock(Connection.class);
        when(dataSource.getConnection()).thenReturn(mockConnection);
        when(mockConnection.isValid(5)).thenReturn(false);
        
        // Execute
        Result<Map<String, Object>> result = healthController.detailedHealth();
        
        // Verify
        assertNotNull(result);
        assertTrue(result.isSuccess());
        assertNotNull(result.getData());
        assertEquals("DOWN", result.getData().get("status"));
        
        @SuppressWarnings("unchecked")
        Map<String, String> databaseStatus = (Map<String, String>) result.getData().get("database");
        assertNotNull(databaseStatus);
        assertEquals("DOWN", databaseStatus.get("status"));
        assertEquals("Database connection is not valid", databaseStatus.get("message"));
        
        verify(mockConnection, times(1)).close();
    }
    
    @Test
    @DisplayName("Test ping endpoint returns pong")
    public void testPing() {
        // Execute
        String result = healthController.ping();
        
        // Verify
        assertEquals("pong", result);
    }
}
