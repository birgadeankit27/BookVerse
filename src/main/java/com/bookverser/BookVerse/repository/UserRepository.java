package com.bookverser.BookVerse.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.bookverser.BookVerse.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

}
