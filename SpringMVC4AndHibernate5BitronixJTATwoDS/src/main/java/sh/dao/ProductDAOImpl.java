package sh.dao;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import sh.model.product.Product;

/**
 *
 * @author Anghel Leonard
 */
@Repository
@Transactional
public class ProductDAOImpl extends GenericDAOImpl<Product, Long> implements ProductDAO {
    
    public ProductDAOImpl() {
        super(Product.class);
    }        
}
