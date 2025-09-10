package com.bookverser.BookVerse.service;

import com.bookverser.BookVerse.dto.SignupDto;
import com.bookverser.BookVerse.dto.UserDto;
import com.bookverser.BookVerse.entity.User;
import com.bookverser.BookVerse.dto.LoginRequest;
import com.bookverser.BookVerse.dto.LoginResponse;

public interface UserService {
    String register(SignupDto signupDto);
    String registerAdmin(SignupDto signupDto);
    User findByEmail(String email);
//    LoginResponse processLogin(LoginRequest loginRequest, String token);
    LoginResponse processLogin(LoginRequest loginRequest, String accessToken, String refreshToken);
    UserDto getUserByEmail(String email);
}