package com.mini12306.service.impl;

import com.mini12306.config.SecurityConfig;
import com.mini12306.dto.AuthRequest;
import com.mini12306.dto.LoginRequest;
import com.mini12306.dto.LoginResponse;
import com.mini12306.dto.RegisterRequest;
import com.mini12306.model.Account;
import com.mini12306.model.Result;
import com.mini12306.repository.AccountRepository;
import com.mini12306.utils.PasswordUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * AuthServiceImpl 单元测试类
 * 主要测试用户注册相关功能
 */
public class AuthServiceImplTest {

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private AuthServiceImpl authService;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        
        // 由于 PasswordUtils 包含静态方法，我们需要使用 PowerMockito 来模拟它
        // 但为了简单起见，这里我们选择不模拟它，而是让测试使用实际的 PasswordUtils 类
        // 在实际项目中，你可能需要使用 PowerMockito 来模拟静态方法
    }

    @Test
    @DisplayName("测试注册成功 - 用户提供完整信息")
    public void testRegisterSuccessWithFullInfo() {
        // 准备测试数据
        RegisterRequest request = new RegisterRequest();
        request.setUsername("testuser");
        request.setPassword("password123");  // 假设这个密码强度足够
        request.setPhone("13800138000");
        request.setRealName("张三");
        request.setCardId("110101199001011234");  // 有效的身份证号
        
        // 模拟 accountRepository.existsByUsername 返回 false，表示用户名不存在
        when(accountRepository.existsByUsername("testuser")).thenReturn(false);
        
        // 模拟 accountRepository.save 返回保存的账户
        Account savedAccount = new Account();
        savedAccount.setId(1L);
        savedAccount.setUsername("testuser");
        savedAccount.setPhone("13800138000");
        savedAccount.setRealName("张三");
        savedAccount.setCardId("110101199001011234");
        savedAccount.setAuthStatus(1);  // 已认证
        
        when(accountRepository.save(any(Account.class))).thenReturn(savedAccount);
        
        // 执行被测试的方法
        Result<Account> result = authService.register(request);
        
        // 验证结果
        assertTrue(result.isSuccess());
        assertEquals("注册成功", result.getMessage());
        assertNotNull(result.getData());
        assertEquals("testuser", result.getData().getUsername());
        assertEquals("张三", result.getData().getRealName());
        assertEquals(1, result.getData().getAuthStatus());
        
        // 验证方法调用
        verify(accountRepository, times(1)).existsByUsername("testuser");
        verify(accountRepository, times(1)).save(any(Account.class));
    }

    @Test
    @DisplayName("测试注册失败 - 用户名已存在")
    public void testRegisterFailureUsernameExists() {
        // 准备测试数据
        RegisterRequest request = new RegisterRequest();
        request.setUsername("existinguser");
        request.setPassword("password123");
        
        // 模拟 accountRepository.existsByUsername 返回 true，表示用户名已存在
        when(accountRepository.existsByUsername("existinguser")).thenReturn(true);
        
        // 执行被测试的方法
        Result<Account> result = authService.register(request);
        
        // 验证结果
        assertFalse(result.isSuccess());
        assertEquals("用户名已存在", result.getMessage());
        assertNull(result.getData());
        
        // 验证方法调用
        verify(accountRepository, times(1)).existsByUsername("existinguser");
        verify(accountRepository, never()).save(any(Account.class));
    }

    @Test
    @DisplayName("测试注册失败 - 密码强度不足")
    public void testRegisterFailureWeakPassword() {
        // 准备测试数据
        RegisterRequest request = new RegisterRequest();
        request.setUsername("testuser");
        request.setPassword("weak");  // 密码强度不足
        
        // 模拟 accountRepository.existsByUsername 返回 false，表示用户名不存在
        when(accountRepository.existsByUsername("testuser")).thenReturn(false);
        
        // 执行被测试的方法
        Result<Account> result = authService.register(request);
        
        // 验证结果
        assertFalse(result.isSuccess());
        assertEquals("密码不能少于6位", result.getMessage());
        assertNull(result.getData());
        
        // 验证方法调用
        verify(accountRepository, times(1)).existsByUsername("testuser");
        verify(accountRepository, never()).save(any(Account.class));
    }

    @Test
    @DisplayName("测试注册失败 - 身份证号格式不正确")
    public void testRegisterFailureInvalidIdCard() {
        // 准备测试数据
        RegisterRequest request = new RegisterRequest();
        request.setUsername("testuser");
        request.setPassword("password123");  // 假设这个密码强度足够
        request.setRealName("张三");
        request.setCardId("1234");  // 无效的身份证号
        
        // 模拟 accountRepository.existsByUsername 返回 false，表示用户名不存在
        when(accountRepository.existsByUsername("testuser")).thenReturn(false);
        
        // 执行被测试的方法
        Result<Account> result = authService.register(request);
        
        // 验证结果
        assertFalse(result.isSuccess());
        assertEquals("身份证号格式不正确", result.getMessage());
        assertNull(result.getData());
        
        // 验证方法调用
        verify(accountRepository, times(1)).existsByUsername("testuser");
        verify(accountRepository, never()).save(any(Account.class));
    }
    
    @Test
    @DisplayName("测试注册成功 - 用户不提供身份信息")
    public void testRegisterSuccessWithoutIdentityInfo() {
        // 准备测试数据
        RegisterRequest request = new RegisterRequest();
        request.setUsername("testuser");
        request.setPassword("password123");  // 假设这个密码强度足够
        request.setPhone("13800138000");
        // 不提供真实姓名和身份证号
        
        // 模拟 accountRepository.existsByUsername 返回 false，表示用户名不存在
        when(accountRepository.existsByUsername("testuser")).thenReturn(false);
        
        // 模拟 accountRepository.save 返回保存的账户
        Account savedAccount = new Account();
        savedAccount.setId(1L);
        savedAccount.setUsername("testuser");
        savedAccount.setPhone("13800138000");
        savedAccount.setAuthStatus(0);  // 未认证
        
        when(accountRepository.save(any(Account.class))).thenReturn(savedAccount);
        
        // 执行被测试的方法
        Result<Account> result = authService.register(request);
        
        // 验证结果
        assertTrue(result.isSuccess());
        assertEquals("注册成功", result.getMessage());
        assertNotNull(result.getData());
        assertEquals("testuser", result.getData().getUsername());
        assertEquals(0, result.getData().getAuthStatus());  // 未认证
        
        // 验证方法调用
        verify(accountRepository, times(1)).existsByUsername("testuser");
        verify(accountRepository, times(1)).save(any(Account.class));
    }

    @Test
    @DisplayName("测试注册 - 只提供真实姓名但不提供身份证号")
    public void testRegisterWithRealNameButNoCardId() {
        // 准备测试数据
        RegisterRequest request = new RegisterRequest();
        request.setUsername("testuser");
        request.setPassword("password123");  // 假设这个密码强度足够
        request.setPhone("13800138000");
        request.setRealName("张三");  // 只提供真实姓名
        request.setCardId(""); // 身份证号为空字符串
        
        // 模拟 accountRepository.existsByUsername 返回 false，表示用户名不存在
        when(accountRepository.existsByUsername("testuser")).thenReturn(false);
        
        // 模拟 accountRepository.save 返回保存的账户
        Account savedAccount = new Account();
        savedAccount.setId(1L);
        savedAccount.setUsername("testuser");
        savedAccount.setPhone("13800138000");
        savedAccount.setRealName("张三");
        savedAccount.setAuthStatus(0);  // 未认证
        
        when(accountRepository.save(any(Account.class))).thenReturn(savedAccount);
        
        // 执行被测试的方法
        Result<Account> result = authService.register(request);
        
        // 验证结果
        assertTrue(result.isSuccess());
        assertEquals("注册成功", result.getMessage());
        assertNotNull(result.getData());
        assertEquals(0, result.getData().getAuthStatus());  // 未认证，因为没有提供完整身份信息
        
        // 验证方法调用
        verify(accountRepository, times(1)).existsByUsername("testuser");
        verify(accountRepository, times(1)).save(any(Account.class));
    }
    
    @Test
    @DisplayName("测试注册 - 只提供身份证号但不提供真实姓名")
    public void testRegisterWithCardIdButNoRealName() {
        // 准备测试数据
        RegisterRequest request = new RegisterRequest();
        request.setUsername("testuser");
        request.setPassword("password123");  // 假设这个密码强度足够
        request.setPhone("13800138000");
        request.setRealName("");  // 真实姓名为空字符串
        request.setCardId("110101199001011234"); // 提供身份证号
        
        // 模拟 accountRepository.existsByUsername 返回 false，表示用户名不存在
        when(accountRepository.existsByUsername("testuser")).thenReturn(false);
        
        // 模拟 accountRepository.save 返回保存的账户
        Account savedAccount = new Account();
        savedAccount.setId(1L);
        savedAccount.setUsername("testuser");
        savedAccount.setPhone("13800138000");
        savedAccount.setCardId("110101199001011234");
        savedAccount.setAuthStatus(0);  // 未认证
        
        when(accountRepository.save(any(Account.class))).thenReturn(savedAccount);
        
        // 执行被测试的方法
        Result<Account> result = authService.register(request);
        
        // 验证结果
        assertTrue(result.isSuccess());
        assertEquals("注册成功", result.getMessage());
        assertNotNull(result.getData());
        assertEquals(0, result.getData().getAuthStatus());  // 未认证，因为没有提供完整身份信息
        
        // 验证方法调用
        verify(accountRepository, times(1)).existsByUsername("testuser");
        verify(accountRepository, times(1)).save(any(Account.class));
    }
    
    @Test
    @DisplayName("测试注册 - 真实姓名和身份证号都只有空格")
    public void testRegisterWithEmptySpaces() {
        // 准备测试数据
        RegisterRequest request = new RegisterRequest();
        request.setUsername("testuser");
        request.setPassword("password123");  // 假设这个密码强度足够
        request.setPhone("13800138000");
        request.setRealName("   ");  // 只有空格
        request.setCardId("   "); // 只有空格
        
        // 模拟 accountRepository.existsByUsername 返回 false，表示用户名不存在
        when(accountRepository.existsByUsername("testuser")).thenReturn(false);
        
        // 模拟 accountRepository.save 返回保存的账户
        Account savedAccount = new Account();
        savedAccount.setId(1L);
        savedAccount.setUsername("testuser");
        savedAccount.setPhone("13800138000");
        savedAccount.setAuthStatus(0);  // 未认证
        
        when(accountRepository.save(any(Account.class))).thenReturn(savedAccount);
        
        // 执行被测试的方法
        Result<Account> result = authService.register(request);
        
        // 验证结果
        assertTrue(result.isSuccess());
        assertEquals("注册成功", result.getMessage());
        assertNotNull(result.getData());
        assertEquals(0, result.getData().getAuthStatus());  // 未认证，因为没有提供有效的身份信息
        
        // 验证方法调用
        verify(accountRepository, times(1)).existsByUsername("testuser");
        verify(accountRepository, times(1)).save(any(Account.class));
    }
    
    @Test
    @DisplayName("测试注册 - 真实姓名和身份证号都为null")
    public void testRegisterWithNullValues() {
        // 准备测试数据
        RegisterRequest request = new RegisterRequest();
        request.setUsername("testuser");
        request.setPassword("password123");  // 假设这个密码强度足够
        request.setPhone("13800138000");
        request.setRealName(null);  // null值
        request.setCardId(null); // null值
        
        // 模拟 accountRepository.existsByUsername 返回 false，表示用户名不存在
        when(accountRepository.existsByUsername("testuser")).thenReturn(false);
        
        // 模拟 accountRepository.save 返回保存的账户
        Account savedAccount = new Account();
        savedAccount.setId(1L);
        savedAccount.setUsername("testuser");
        savedAccount.setPhone("13800138000");
        savedAccount.setAuthStatus(0);  // 未认证
        
        when(accountRepository.save(any(Account.class))).thenReturn(savedAccount);
        
        // 执行被测试的方法
        Result<Account> result = authService.register(request);
        
        // 验证结果
        assertTrue(result.isSuccess());
        assertEquals("注册成功", result.getMessage());
        assertNotNull(result.getData());
        assertEquals(0, result.getData().getAuthStatus());  // 未认证，因为没有提供身份信息
        
        // 验证方法调用
        verify(accountRepository, times(1)).existsByUsername("testuser");
        verify(accountRepository, times(1)).save(any(Account.class));
    }

    /**
     * 测试验证身份证号方法
     * 由于validateIdCard是私有方法，我们需要使用反射来测试它
     */
    @Test
    @DisplayName("测试身份证号验证")
    public void testValidateIdCard() {
        // 使用反射调用私有方法
        Object result15 = ReflectionTestUtils.invokeMethod(authService, "validateIdCard", "110101900101123");
        Object result18 = ReflectionTestUtils.invokeMethod(authService, "validateIdCard", "110101199001011234");
        Object resultX = ReflectionTestUtils.invokeMethod(authService, "validateIdCard", "11010119900101123X");
        Object resultShort = ReflectionTestUtils.invokeMethod(authService, "validateIdCard", "1101011990010");
        Object resultLong = ReflectionTestUtils.invokeMethod(authService, "validateIdCard", "1101011990010112345");
        Object resultChar = ReflectionTestUtils.invokeMethod(authService, "validateIdCard", "11010119900101A23");
        
        // 验证结果
        assertTrue(Boolean.TRUE.equals(result15), "15位身份证应该有效");
        assertTrue(Boolean.TRUE.equals(result18), "18位身份证应该有效");
        assertTrue(Boolean.TRUE.equals(resultX), "18位身份证末位为X应该有效");
        assertTrue(Boolean.FALSE.equals(resultShort), "少于15位的身份证应该无效");
        assertTrue(Boolean.FALSE.equals(resultLong), "多于18位的身份证应该无效");
        assertTrue(Boolean.FALSE.equals(resultChar), "包含非法字符的身份证应该无效");
    }
    
    @Test
    @DisplayName("测试登录成功")
    public void testLoginSuccess() {
        // 准备测试数据
        LoginRequest request = new LoginRequest();
        request.setUsername("testuser");
        request.setPassword("password123");
        
        // 模拟账户查询
        Account account = new Account();
        account.setId(1L);
        account.setUsername("testuser");
        account.setPassword(PasswordUtils.encrypt("password123")); // 使用真实加密方法
        account.setAuthStatus(1); // 已认证
        
        when(accountRepository.findByUsername("testuser")).thenReturn(Optional.of(account));
        
        // 模拟令牌生成
        try (MockedStatic<SecurityConfig> mockedSecurityConfig = org.mockito.Mockito.mockStatic(SecurityConfig.class)) {
            mockedSecurityConfig.when(() -> SecurityConfig.generateToken(1L)).thenReturn("test-token-123");
            
            // 执行被测试的方法
            Result<LoginResponse> result = authService.login(request);
            
            // 验证结果
            assertTrue(result.isSuccess());
            assertEquals("登录成功", result.getMessage());
            assertNotNull(result.getData());
            assertEquals(1L, result.getData().getUserId());
            assertEquals("testuser", result.getData().getUsername());
            assertEquals("test-token-123", result.getData().getToken());
            assertEquals(1, result.getData().getAuthStatus());
            
            // 验证方法调用
            verify(accountRepository, times(1)).findByUsername("testuser");
        }
    }
    
    @Test
    @DisplayName("测试登录失败 - 用户名为空")
    public void testLoginFailureEmptyUsername() {
        // 准备测试数据
        LoginRequest request = new LoginRequest();
        request.setUsername("");
        request.setPassword("password123");
        
        // 执行被测试的方法
        Result<LoginResponse> result = authService.login(request);
        
        // 验证结果
        assertFalse(result.isSuccess());
        assertEquals("用户名不能为空", result.getMessage());
        assertNull(result.getData());
        
        // 验证方法调用 - 不应该查询数据库
        verify(accountRepository, never()).findByUsername(anyString());
    }
    
    @Test
    @DisplayName("测试登录失败 - 密码为空")
    public void testLoginFailureEmptyPassword() {
        // 准备测试数据
        LoginRequest request = new LoginRequest();
        request.setUsername("testuser");
        request.setPassword("");
        
        // 执行被测试的方法
        Result<LoginResponse> result = authService.login(request);
        
        // 验证结果
        assertFalse(result.isSuccess());
        assertEquals("密码不能为空", result.getMessage());
        assertNull(result.getData());
        
        // 验证方法调用 - 不应该查询数据库
        verify(accountRepository, never()).findByUsername(anyString());
    }
    
    @Test
    @DisplayName("测试登录失败 - 用户名为null")
    public void testLoginFailureNullUsername() {
        // 准备测试数据
        LoginRequest request = new LoginRequest();
        request.setUsername(null); // 用户名为null
        request.setPassword("password123");
        
        // 执行被测试的方法
        Result<LoginResponse> result = authService.login(request);
        
        // 验证结果
        assertFalse(result.isSuccess());
        assertEquals("用户名不能为空", result.getMessage());
        assertNull(result.getData());
        
        // 验证方法调用 - 不应该查询数据库
        verify(accountRepository, never()).findByUsername(anyString());
    }
    
    @Test
    @DisplayName("测试登录失败 - 用户名只包含空格")
    public void testLoginFailureUsernameOnlySpaces() {
        // 准备测试数据
        LoginRequest request = new LoginRequest();
        request.setUsername("   "); // 用户名只包含空格
        request.setPassword("password123");
        
        // 执行被测试的方法
        Result<LoginResponse> result = authService.login(request);
        
        // 验证结果
        assertFalse(result.isSuccess());
        assertEquals("用户名不能为空", result.getMessage());
        assertNull(result.getData());
        
        // 验证方法调用 - 不应该查询数据库
        verify(accountRepository, never()).findByUsername(anyString());
    }
    
    @Test
    @DisplayName("测试登录失败 - 密码为null")
    public void testLoginFailureNullPassword() {
        // 准备测试数据
        LoginRequest request = new LoginRequest();
        request.setUsername("testuser");
        request.setPassword(null); // 密码为null
        
        // 执行被测试的方法
        Result<LoginResponse> result = authService.login(request);
        
        // 验证结果
        assertFalse(result.isSuccess());
        assertEquals("密码不能为空", result.getMessage());
        assertNull(result.getData());
        
        // 验证方法调用 - 不应该查询数据库
        verify(accountRepository, never()).findByUsername(anyString());
    }
    
    @Test
    @DisplayName("测试登录失败 - 密码只包含空格")
    public void testLoginFailurePasswordOnlySpaces() {
        // 准备测试数据
        LoginRequest request = new LoginRequest();
        request.setUsername("testuser");
        request.setPassword("   "); // 密码只包含空格
        
        // 执行被测试的方法
        Result<LoginResponse> result = authService.login(request);
        
        // 验证结果
        assertFalse(result.isSuccess());
        assertEquals("密码不能为空", result.getMessage());
        assertNull(result.getData());
        
        // 验证方法调用 - 不应该查询数据库
        verify(accountRepository, never()).findByUsername(anyString());
    }
    
    @Test
    @DisplayName("测试登录失败 - 用户不存在")
    public void testLoginFailureUserNotExists() {
        // 准备测试数据
        LoginRequest request = new LoginRequest();
        request.setUsername("nonexistentuser");
        request.setPassword("password123");
        
        // 模拟账户查询 - 用户不存在
        when(accountRepository.findByUsername("nonexistentuser")).thenReturn(Optional.empty());
        
        // 执行被测试的方法
        Result<LoginResponse> result = authService.login(request);
        
        // 验证结果
        assertFalse(result.isSuccess());
        assertEquals("用户名或密码错误", result.getMessage());
        assertNull(result.getData());
        
        // 验证方法调用
        verify(accountRepository, times(1)).findByUsername("nonexistentuser");
    }
    
    @Test
    @DisplayName("测试登录失败 - 密码错误")
    public void testLoginFailureWrongPassword() {
        // 准备测试数据
        LoginRequest request = new LoginRequest();
        request.setUsername("testuser");
        request.setPassword("wrongpassword");
        
        // 模拟账户查询
        Account account = new Account();
        account.setId(1L);
        account.setUsername("testuser");
        account.setPassword(PasswordUtils.encrypt("correctpassword")); // 使用真实加密方法
        
        when(accountRepository.findByUsername("testuser")).thenReturn(Optional.of(account));
        
        // 执行被测试的方法
        Result<LoginResponse> result = authService.login(request);
        
        // 验证结果
        assertFalse(result.isSuccess());
        assertEquals("用户名或密码错误", result.getMessage());
        assertNull(result.getData());
        
        // 验证方法调用
        verify(accountRepository, times(1)).findByUsername("testuser");
    }
    
    @Test
    @DisplayName("测试身份认证成功")
    public void testAuthenticateSuccess() {
        // 准备测试数据
        Long userId = 1L;
        AuthRequest request = new AuthRequest();
        request.setRealName("张三");
        request.setCardId("110101199001011234");
        
        // 模拟账户查询
        Account account = new Account();
        account.setId(userId);
        account.setUsername("testuser");
        account.setAuthStatus(0); // 未认证
        
        when(accountRepository.findById(userId)).thenReturn(Optional.of(account));
        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        // 执行被测试的方法
        Result<Account> result = authService.authenticate(userId, request);
        
        // 验证结果
        assertTrue(result.isSuccess());
        assertEquals("身份认证成功", result.getMessage());
        assertNotNull(result.getData());
        assertEquals("张三", result.getData().getRealName());
        assertEquals("110101199001011234", result.getData().getCardId());
        assertEquals(1, result.getData().getAuthStatus()); // 已认证
        assertNotNull(result.getData().getUpdateTime()); // 更新时间不为空
        
        // 验证方法调用
        verify(accountRepository, times(1)).findById(userId);
        verify(accountRepository, times(1)).save(any(Account.class));
    }
    
    @Test
    @DisplayName("测试身份认证失败 - 用户不存在")
    public void testAuthenticateFailureUserNotExists() {
        // 准备测试数据
        Long userId = 999L;
        AuthRequest request = new AuthRequest();
        request.setRealName("张三");
        request.setCardId("110101199001011234");
        
        // 模拟账户查询 - 用户不存在
        when(accountRepository.findById(userId)).thenReturn(Optional.empty());
        
        // 执行被测试的方法
        Result<Account> result = authService.authenticate(userId, request);
        
        // 验证结果
        assertFalse(result.isSuccess());
        assertEquals("用户不存在", result.getMessage());
        assertNull(result.getData());
        
        // 验证方法调用
        verify(accountRepository, times(1)).findById(userId);
        verify(accountRepository, never()).save(any(Account.class));
    }
    
    @Test
    @DisplayName("测试获取用户信息成功")
    public void testGetUserInfoSuccess() {
        // 准备测试数据
        Long userId = 1L;
        
        // 模拟账户查询
        Account account = new Account();
        account.setId(userId);
        account.setUsername("testuser");
        account.setRealName("张三");
        account.setCardId("110101199001011234");
        account.setPhone("13800138000");
        account.setAuthStatus(1); // 已认证
        
        when(accountRepository.findById(userId)).thenReturn(Optional.of(account));
        
        // 执行被测试的方法
        Result<Account> result = authService.getUserInfo(userId);
        
        // 验证结果
        assertTrue(result.isSuccess());
        assertNotNull(result.getData());
        assertEquals(userId, result.getData().getId());
        assertEquals("testuser", result.getData().getUsername());
        assertEquals("张三", result.getData().getRealName());
        assertEquals("110101199001011234", result.getData().getCardId());
        assertEquals("13800138000", result.getData().getPhone());
        assertEquals(1, result.getData().getAuthStatus());
        
        // 验证方法调用
        verify(accountRepository, times(1)).findById(userId);
    }
    
    @Test
    @DisplayName("测试获取用户信息失败 - 用户不存在")
    public void testGetUserInfoFailureUserNotExists() {
        // 准备测试数据
        Long userId = 999L;
        
        // 模拟账户查询 - 用户不存在
        when(accountRepository.findById(userId)).thenReturn(Optional.empty());
        
        // 执行被测试的方法
        Result<Account> result = authService.getUserInfo(userId);
        
        // 验证结果
        assertFalse(result.isSuccess());
        assertEquals("用户不存在", result.getMessage());
        assertNull(result.getData());
        
        // 验证方法调用
        verify(accountRepository, times(1)).findById(userId);
    }
}
