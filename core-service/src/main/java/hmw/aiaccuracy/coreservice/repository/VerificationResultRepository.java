package hmw.aiaccuracy.coreservice.repository;

import hmw.aiaccuracy.coreservice.domain.VerificationResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VerificationResultRepository extends JpaRepository<VerificationResult, String> {
}
