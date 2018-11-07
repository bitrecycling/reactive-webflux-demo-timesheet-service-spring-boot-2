package de.bitrecycling.timeshizz;

import de.bitrecycling.timeshizz.client.model.Client;
import de.bitrecycling.timeshizz.client.repository.ClientRepository;
import de.bitrecycling.timeshizz.project.model.Project;
import de.bitrecycling.timeshizz.project.repository.ProjectRespository;
import de.bitrecycling.timeshizz.task.model.Task;
import de.bitrecycling.timeshizz.task.model.TaskEntry;
import de.bitrecycling.timeshizz.task.repository.TaskEntryRepository;
import de.bitrecycling.timeshizz.task.repository.TaskRepository;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = TimeshizzApplication.class)
@AutoConfigureMockMvc
public class RepositoryTests {

    @Autowired
    ClientRepository clientRepository;
    @Autowired
    ProjectRespository projectRespository;
    @Autowired
    TaskRepository taskRepository;
    @Autowired
    TaskEntryRepository taskEntryRepository;
    @Autowired
    org.springframework.data.mongodb.core.MongoTemplate mongoTemplate;


    @After
    public void teardown() {
        mongoTemplate.getDb().drop();
    }


    @Test
    public void fullTurnaround() {
        Client c = new Client("fullTestClient","fullTestClientAddress");
        c.setId("clientId");
        clientRepository.insert(c).block();
        Project p = new Project("fullTestProjectName","fullTestProjectDescription",60.0,c.getId());
        p.setId("projectId");
        projectRespository.insert(p).block();
        Task t = new Task("fullTestTaskName",p.getId());
        t.setId("taskId");
        taskRepository.insert(t).block();
        TaskEntry te1 = new TaskEntry(LocalDateTime.now(),120, t.getId());
        TaskEntry te2 = new TaskEntry(LocalDateTime.now(),180, t.getId());
        taskEntryRepository.insert(te1).block();
        taskEntryRepository.insert(te2).block();
        StepVerifier.create(clientRepository.findAll()).expectNextMatches(
                client -> {
                   return c.equals(client);
                }).verifyComplete();

    }

    @Test
    public void testProjectsByClient() {
        Client c = createTestData();
        StepVerifier.create(projectRespository.findAllByClientId(c.getId()))
                .expectNextMatches(
                        project -> project.getName().equals("fullTestProjectName"))
                .verifyComplete();
    }

    @Test
    public void testTasks(){
        LocalDateTime before = LocalDateTime.now().minusSeconds(2);
        Task t1 = new Task("t1", "pid1");
        Task t2 = new Task("t2", "pid1");
        Task t3 = new Task("t3", "pid2");
        Task t4 = new Task("t4", "pid2");
        Task t5 = new Task("t5", "pid2");
        List<Task> tasks = Arrays.asList(t1, t2, t3, t4, t5);
        tasks.forEach(task -> taskRepository.insert(task).block());
        StepVerifier.create(taskRepository.findAllByProjectId("pid1")).expectNextCount(2).verifyComplete();
        StepVerifier.create(taskRepository.findAllByProjectId("pid2")).expectNextCount(3).verifyComplete();
        StepVerifier.create(taskRepository.findByCreationTimeBetween(before, LocalDateTime.now()))
                .expectNextCount(5).verifyComplete();
        StepVerifier.create(taskRepository.findByCreationTimeBetween(before, before))
                .expectNextCount(0).verifyComplete();
        t1.setCreationTime(LocalDateTime.now().minusMinutes(1));
        t2.setCreationTime(LocalDateTime.now().minusMinutes(1));
        t5.setCreationTime(LocalDateTime.now().minusMinutes(1));
        tasks.forEach(task -> taskRepository.save(task).block());
        StepVerifier.create(taskRepository.findByCreationTimeBetween(before.minusMinutes(1), before))
                .expectNextCount(3).verifyComplete();
    }

    private Client createTestData() {
        Client c = new Client("fullTestClient","fullTestClientAddress");
        c.setId("clientId");
        clientRepository.insert(c).block();
        Project p = new Project("fullTestProjectName","fullTestProjectDescription",60.0,c.getId());
        p.setId("projectId");
        projectRespository.insert(p).block();
        Task t = new Task("fullTestTaskName",p.getId());
        t.setId("taskId");
        taskRepository.insert(t).block();
        TaskEntry te1 = new TaskEntry(LocalDateTime.now(),120, t.getId());
        TaskEntry te2 = new TaskEntry(LocalDateTime.now(),180, t.getId());
        taskEntryRepository.insert(te1).block();
        taskEntryRepository.insert(te2).block();
        return c;
    }
}
