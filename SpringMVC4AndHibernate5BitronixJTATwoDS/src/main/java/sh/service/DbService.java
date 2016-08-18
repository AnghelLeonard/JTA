package sh.service;

import org.springframework.stereotype.Service;
import sh.exception.PersistException;
import sh.model.detail.Detail;
import sh.model.product.Product;

/**
 *
 * @author Anghel Leonard
 */
@Service
public interface DbService {
        
    // persist successfully
    public void persistToDatabase(Product p, Detail d);
    
    // rollback both databases
    public void persistToDatabaseWithException(Product p, Detail d) throws PersistException;
}
