package com.example.demo.database.repositories.vehicle;

import com.example.demo.database.models.vehicle.FileByteData;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
@Qualifier("file_byte_data")
public interface FileByteDataRepository extends JpaRepository<FileByteData, Long> {
}
