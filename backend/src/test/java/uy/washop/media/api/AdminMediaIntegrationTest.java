package uy.washop.media.api;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import uy.washop.auth.domain.User;
import uy.washop.auth.domain.UserRole;
import uy.washop.auth.infrastructure.UserRepository;
import uy.washop.security.AdminUserDetails;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AdminMediaIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private AdminUserDetails adminDetails;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        User admin = new User();
        admin.setEmail("media-admin@washop.uy");
        admin.setPasswordHash(passwordEncoder.encode("ChangeMe123!"));
        admin.setFirstName("Media");
        admin.setLastName("Admin");
        admin.setRole(UserRole.ADMIN);
        admin.setEnabled(true);
        adminDetails = new AdminUserDetails(userRepository.save(admin));
    }

    @Test
    void uploadAcceptsPngAndRejectsNonImage() throws Exception {
        byte[] png = new byte[] {
                (byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A,
                0, 0, 0, 0, 0, 0, 0, 0
        };
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "logo.png",
                MediaType.APPLICATION_OCTET_STREAM_VALUE,
                png
        );

        String body = mockMvc.perform(multipart("/api/admin/media/upload")
                        .file(file)
                        .param("folder", "logos")
                        .with(user(adminDetails))
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.provider").value("local"))
                .andExpect(jsonPath("$.publicId").exists())
                .andExpect(jsonPath("$.url").exists())
                .andExpect(jsonPath("$.format").value("png"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        String publicId = com.jayway.jsonpath.JsonPath.read(body, "$.publicId");

        mockMvc.perform(delete("/api/admin/media")
                        .with(user(adminDetails))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"publicId\":\"" + publicId + "\"}"))
                .andExpect(status().isNoContent());

        MockMultipartFile bad = new MockMultipartFile(
                "file",
                "notes.txt",
                "text/plain",
                "hello".getBytes()
        );
        mockMvc.perform(multipart("/api/admin/media/upload")
                        .file(bad)
                        .with(user(adminDetails))
                        .with(csrf()))
                .andExpect(status().isConflict());
    }
}
