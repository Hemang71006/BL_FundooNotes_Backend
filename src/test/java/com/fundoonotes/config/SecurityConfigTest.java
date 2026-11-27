
package com.fundoonotes.config;

import com.fundoonotes.controller.AuthController;
import com.fundoonotes.controller.BatchController;
import com.fundoonotes.entity.User;
import com.fundoonotes.repository.UserRepository;
import com.fundoonotes.security.JwtAuthenticationFilter;
import com.fundoonotes.security.JwtService;
import com.fundoonotes.service.AuthService;
import com.fundoonotes.service.NoteExcelService;

import com.fundoonotes.dto.request.LoginRequest;
import com.fundoonotes.dto.response.LoginResponse;

import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.job.Job;
import org.springframework.batch.core.job.JobExecution;
import org.springframework.batch.core.job.parameters.JobParametersBuilder;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.batch.core.repository.JobRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.mock.web.MockMultipartFile;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import com.fundoonotes.redis.RedisService;

/*
 * Project Name : Fundoo Notes Backend
 * Description  : Verifies public endpoints, JWT protection and CORS behavior.
 * Author       : Hemang
 * Date         : 27-11-2026
 */

@WebMvcTest({AuthController.class, BatchController.class})
@Import({SecurityConfig.class, JwtAuthenticationFilter.class})
@AutoConfigureMockMvc(addFilters = false)
class SecurityConfigTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @BeforeEach
    void setUpMockMvc() {

        mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();
    }

    @MockitoBean
    private AuthService authService;

    @MockitoBean
    private RedisService redisService;

    // Mock the JWT service used internally by the real JWT filter.
    @MockitoBean
    private JwtService jwtService;

    // Mock the repository used internally by the real JWT filter.
    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private JobOperator jobOperator;

    @MockitoBean
    private JobRepository jobRepository;

    @MockitoBean(name = "importNotesJob")
    private Job importNotesJob;

    @MockitoBean
    private NoteExcelService noteExcelService;

    @Test
    void loginEndpointShouldBePublic() throws Exception {

        LoginResponse response = new LoginResponse(
                "test-jwt-token",
                "Bearer",
                "hemang@example.com",
                "Login successful"
        );

        when(authService.login(any(LoginRequest.class)))
                .thenReturn(response);

        String requestBody = """
                {
                    "email": "hemang@example.com",
                    "password": "Password@123"
                }
                """;

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk());
    }

    @Test
    void protectedEndpointShouldRejectRequestWithoutToken() throws Exception {

        mockMvc.perform(get("/api/notes"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void reminderEndpointsShouldRequireAuthentication() throws Exception {

        mockMvc.perform(get("/api/notes/reminders"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void batchEndpointsShouldRequireAuthentication() throws Exception {

        mockMvc.perform(get("/api/batch/export/1"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(multipart("/api/batch/import")
                        .file(new MockMultipartFile(
                                "file",
                                "notes.xlsx",
                                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                                new byte[]{1})))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/batch/status/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void corsPreflightShouldRejectOtherOrigins() throws Exception {

        mockMvc.perform(options("/api/notes")
                        .header("Origin", "http://localhost:3000")
                        .header("Access-Control-Request-Method", "GET"))
                .andExpect(status().isForbidden())
                .andExpect(header().doesNotExist(
                        "Access-Control-Allow-Origin"));
    }

    @Test
    @WithMockUser(username = "owner@example.com")
    void authenticatedRequestFromConfiguredOriginIncludesCorsHeader()
            throws Exception {

        User user = new User();
        user.setId(7L);
        user.setEmail("owner@example.com");
        user.setPassword("encoded-password");

        when(userRepository.findByEmail("owner@example.com"))
                .thenReturn(java.util.Optional.of(user));

        JobExecution execution = mock(JobExecution.class);
        when(execution.getId()).thenReturn(12L);
        when(execution.getStatus()).thenReturn(BatchStatus.COMPLETED);
        when(execution.getExitStatus()).thenReturn(ExitStatus.COMPLETED);
        when(execution.getJobParameters()).thenReturn(
                new JobParametersBuilder()
                        .addLong("userId", 7L)
                        .toJobParameters());
        when(jobRepository.getJobExecution(12L)).thenReturn(execution);

        mockMvc.perform(get("/api/batch/status/12")
                        .header("Origin", "http://localhost:5173"))
                .andExpect(status().isOk())
                .andExpect(header().string(
                        "Access-Control-Allow-Origin",
                        "http://localhost:5173"));
    }

    @Test
    void corsPreflightShouldBeAllowedForConfiguredFrontend() throws Exception {

        mockMvc.perform(options("/api/notes")
                        .header("Origin", "http://localhost:5173")
                        .header("Access-Control-Request-Method", "GET")
                        .header("Access-Control-Request-Headers", "Authorization,Content-Type"))
                .andExpect(status().isOk())
                .andExpect(header().string(
                        "Access-Control-Allow-Origin",
                        "http://localhost:5173"
                ))
                .andExpect(header().string(
                        "Access-Control-Allow-Credentials",
                        org.hamcrest.Matchers.nullValue()
                ));
    }
}
