package com.eduardolima.todolist.filter;

import java.io.IOException;
import java.util.Base64;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.eduardolima.todolist.user.IUserRepository;

import at.favre.lib.crypto.bcrypt.BCrypt;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class FilterTaskAuth extends OncePerRequestFilter {

    @Autowired
    IUserRepository iUserRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws IOException, ServletException {

        // deve realizar a validacao de autenticacao somente na rota /tasks/
        var servletPath = request.getServletPath();

        if(servletPath.startsWith("/tasks/")){

                // Pegar a autenticacao - usuario e senha
            var authorization = request.getHeader("Authorization");

                // capturar somente o token
            var authEncoded = authorization.substring(5).trim();

                // converter de BASE64 para byte
            byte[] authDecoded = Base64.getDecoder().decode(authEncoded);

                // converter de byte para String
            var authString = new String(authDecoded);

                // separar username e password
            String[] credentials = authString.split(":");
            String username = credentials[0];
            String password = credentials[1];

            // Validar se usuario existe
            var user = iUserRepository.findByUsername(username);
            
            if(user == null){
                response.sendError(401);
            } else {

            // Validar se senha está correta
            var passwordVerify = BCrypt.verifyer().verify(password.toCharArray(), user.getPassword());

                // se a senha é válida entao permitir a requisicao, senao retornar 401
                if(passwordVerify.verified){
                    // setar o idUser para capturar do token
                    request.setAttribute("idUser", user.getId());
                    filterChain.doFilter(request, response);
                } else {
                    response.sendError(401);
                }
                
            }
        } else {
            filterChain.doFilter(request, response);
        }

    }
    
}
