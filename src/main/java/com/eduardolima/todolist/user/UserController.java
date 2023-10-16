package com.eduardolima.todolist.user;

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

import at.favre.lib.crypto.bcrypt.BCrypt;


@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private IUserRepository iUserRepository;

    @PostMapping("/")
    // requisicao post de criacao, que deve passar os atributos da classe userModel como body
    public ResponseEntity created(@RequestBody UserModel userModel){

        // retorna o userModel que possua o username passado na requisicao 
        var user = iUserRepository.findByUsername(userModel.getUsername());

        // check if the username exists, if exists will return status code 400
        if(user != null){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Usuário já existente.");
        }

        // criptograr senha e setar
        var passwordHashed = BCrypt.withDefaults().hashToString(12, userModel.getPassword().toCharArray());
        userModel.setPassword(passwordHashed);

        // salva a informacao no banco de dados
        var userCreated = iUserRepository.save(userModel);

        // retorna body do userCreated
        // retorna também o status code 201 - created      
        return ResponseEntity.status(HttpStatus.CREATED).body(userCreated);
    }

    @GetMapping("/")
    public ResponseEntity gettAllUsers(){

        var getUsers = iUserRepository.findAll();

        return ResponseEntity.status(HttpStatus.OK).body(getUsers);

    }

    @GetMapping("/{id}")
    public ResponseEntity getUser(@PathVariable UUID id){

        var user = iUserRepository.findById(id).orElse(null);

        if(user == null){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Não foi encontrado usuário com o id informado.");
        }

        return ResponseEntity.status(HttpStatus.OK).body(user);

    }

    @PutMapping("/{id}")
    public ResponseEntity update(@RequestBody UserModel userModel, @PathVariable UUID id){

        var user = iUserRepository.findById(id).orElse(null);

        if(user == null){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Não foi encontrado usuário com o id informado.");
        }

        Utils.copyNonNullProperties(userModel, user);

        var userUpdated = iUserRepository.save(user);

        return ResponseEntity.status(HttpStatus.OK).body(userUpdated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity delete(@PathVariable UUID id){

        var user = iUserRepository.findById(id).orElse(null);

        if(user == null){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Não foi encontrado usuário com o id informado.");
        }

        iUserRepository.delete(user);

        return ResponseEntity.status(HttpStatus.OK).body("Usuário removido com sucesso.");

    }
    
}
