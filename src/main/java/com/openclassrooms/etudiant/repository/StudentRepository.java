package com.openclassrooms.etudiant.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.openclassrooms.etudiant.entities.Student;
import java.util.Optional;

public interface StudentRepository extends JpaRepository<Student, Long> {
    Optional<Student> findByEmail(String email);
}

