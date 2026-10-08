package au.edu.uow.csci318.assessment.controller;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import au.edu.uow.csci318.assessment.application.AssessmentApplicationService;
import au.edu.uow.csci318.assessment.domain.Assessment.Priority;
import au.edu.uow.csci318.assessment.domain.Assessment.Status;
import au.edu.uow.csci318.assessment.dto.AssessmentDtos.CreateRequest;
import au.edu.uow.csci318.assessment.dto.AssessmentDtos.Response;
import au.edu.uow.csci318.assessment.infrastructure.IdentityClient;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class AssessmentControllerApiTest {
  @Test
  void createsAssessmentThroughAuthenticatedApi() throws Exception {
    AssessmentApplicationService service = mock(AssessmentApplicationService.class);
    IdentityClient identity = mock(IdentityClient.class);
    UUID owner = UUID.randomUUID();
    UUID subject = UUID.randomUUID();
    UUID assessment = UUID.randomUUID();
    LocalDate due = LocalDate.of(2026, 10, 28);
    CreateRequest request =
        new CreateRequest(subject, "Report", "Report", 40.0, due, null, null, 300, Priority.HIGH);
    when(identity.require("Bearer test")).thenReturn(owner);
    when(service.create(eq(owner), eq("Bearer test"), eq(request)))
        .thenReturn(
            new Response(
                assessment,
                subject,
                "Report",
                "Report",
                40.0,
                due,
                null,
                null,
                300,
                Priority.HIGH,
                Status.INCOMPLETE,
                Instant.parse("2026-10-07T00:00:00Z")));
    MockMvc api = MockMvcBuilders.standaloneSetup(new AssessmentController(service, identity)).build();

    api.perform(
            post("/api/assessments")
                .header("Authorization", "Bearer test")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {"subjectId":"%s","title":"Report","type":"Report","weighting":40,
                     "dueDate":"2026-10-28","estimatedMinutes":300,"priority":"HIGH"}
                    """
                        .formatted(subject)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.status").value("INCOMPLETE"));

    verify(service).create(owner, "Bearer test", request);
  }
}
