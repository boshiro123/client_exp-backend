package back.client_exp_backend.repository;

import back.client_exp_backend.models.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ClientRepository extends JpaRepository<Client, Long> {
  Optional<Client> findByEmail(String email);

  boolean existsByEmail(String email);
}