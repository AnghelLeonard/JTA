package sh.service;

import org.springframework.stereotype.Service;

/**
 *
 * @author Anghel Leonard
 */
@Service
public interface ProductService {
    
    public void productToDatabaseTransactionTemplate();
    public void productToDatabaseTransactional();
}
