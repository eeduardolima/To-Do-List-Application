package com.eduardolima.todolist.task;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.eduardolima.todolist.utils.Utils;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/tasks")
public class TaskController {

    @Autowired
    ITaskRepository iTaskRepository;


    @PostMapping("/")
    public ResponseEntity create(@RequestBody TaskModel taskModel, HttpServletRequest request){

        // setar o iduser capturado da autenticacao, dentro da classe filter

        var idUser = (UUID)request.getAttribute("idUser");
        taskModel.setIdUser(idUser);

        // validar se a data inicial e data final é maior que a data atual
        var currentData = LocalDateTime.now();
        if(currentData.isAfter(taskModel.getStartAt()) || currentData.isAfter(taskModel.getEndAt())){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("A data inicial e de término deve ser maior que a data atual.");
        }

        // validar que a data inicial é menor que a data final
        if(taskModel.getStartAt().isAfter(taskModel.getEndAt())){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("A data de término deve ser maior que a data inicial.");
        }

        var taskCreated = iTaskRepository.save(taskModel);

        return ResponseEntity.status(HttpStatus.CREATED).body(taskCreated);
    }

    @GetMapping("/")
    public List<TaskModel> list(HttpServletRequest request){

        var idUser = (UUID)request.getAttribute("idUser");
        var tasks = iTaskRepository.findByIdUser(idUser);

        return tasks;
    }

    @PutMapping("/{id}")
    public ResponseEntity update(@RequestBody TaskModel taskModel, HttpServletRequest request, @PathVariable UUID id){

        var task = iTaskRepository.findById(id).orElse(null);

        // caso a tarefa nao existir, retornar status code 400
        if (task == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Tarefa não encontrada.");
        }
        // validar se o usuario da requisicao é o mesmo usuário criador da tarefa
        var idUser = request.getAttribute("idUser");
        if (!task.getIdUser().equals(idUser)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Usuário não possui permissão para editar essa tarefa.");
        }

        Utils.copyNonNullProperties(taskModel, task);

        var taskUpdated = iTaskRepository.save(task);

        return ResponseEntity.status(HttpStatus.OK).body(taskUpdated);

    }

    @DeleteMapping("/{id}")
    public ResponseEntity delete(HttpServletRequest request, @PathVariable UUID id){

        var task = iTaskRepository.findById(id).orElse(null);

        if (task == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Tarefa não encontrada.");
        }

        var idUser = request.getAttribute("idUser");
        if (!task.getIdUser().equals(idUser)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Usuário não possui permissão para editar essa tarefa.");
        }

        iTaskRepository.delete(task);

        return ResponseEntity.status(HttpStatus.OK).body("Tarefa excluída com sucesso.");
    }
    
}
