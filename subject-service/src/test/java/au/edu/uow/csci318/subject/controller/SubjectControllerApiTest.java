package au.edu.uow.csci318.subject.controller;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import au.edu.uow.csci318.subject.application.SubjectApplicationService;
import au.edu.uow.csci318.subject.dto.SubjectDtos.SubjectResponse;
import au.edu.uow.csci318.subject.dto.SubjectDtos.UpdateSubjectRequest;
import au.edu.uow.csci318.subject.exception.ApiExceptionHandler;
import au.edu.uow.csci318.subject.infrastructure.IdentityClient;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class SubjectControllerApiTest {
  private final SubjectApplicationService service = mock(SubjectApplicationService.class);
  private final IdentityClient identity = mock(IdentityClient.class);
  private final UUID ownerId = UUID.randomUUID();
  private final UUID subjectId = UUID.randomUUID();
  private MockMvc api;

  @BeforeEach
  void setUp() {
    api =
        MockMvcBuilders.standaloneSetup(new SubjectController(service, identity))
            .setControllerAdvice(new ApiExceptionHandler())
            .build();
    when(identity.require("Bearer test")).thenReturn(ownerId);
  }

  @Test
  void editsAllSubjectDetailsThroughAuthenticatedApi() throws Exception {
    UpdateSubjectRequest request =
        new UpdateSubjectRequest("CSCI399", "Capstone", 12, 360);
    when(service.update(eq(ownerId), eq(subjectId), eq(request)))
        .thenReturn(new SubjectResponse(subjectId, "CSCI399", "Capstone", 12, 360));

    api.perform(
            patch("/api/subjects/{id}", subjectId)
                .header("Authorization", "Bearer test")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {"code":"CSCI399","name":"Capstone","creditPoints":12,
                     "weeklyStudyTargetMinutes":360}
                    """))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.code").value("CSCI399"))
        .andExpect(jsonPath("$.weeklyStudyTargetMinutes").value(360));

    verify(service).update(ownerId, subjectId, request);
  }

  @Test
  void rejectsInvalidEditableSubjectBeforeApplicationService() throws Exception {
    api.perform(
            patch("/api/subjects/{id}", subjectId)
                .header("Authorization", "Bearer test")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {"code":"","name":"","creditPoints":0,
                     "weeklyStudyTargetMinutes":10081}
                    """))
        .andExpect(status().isBadRequest());
  }
}
