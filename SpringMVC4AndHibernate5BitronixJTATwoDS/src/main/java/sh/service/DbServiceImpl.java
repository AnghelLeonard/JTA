package sh.service;

import com.vladmihalcea.sql.SQLStatementCountValidator;
import static com.vladmihalcea.sql.SQLStatementCountValidator.assertInsertCount;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.annotation.Transactional;
import sh.dao.DetailDAO;
import sh.dao.ProductDAO;
import sh.exception.PersistException;
import sh.model.detail.Detail;
import sh.model.product.Product;

/**
 *
 * @author Anghel Leonard
 */
@Repository
public class DbServiceImpl implements DbService {

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(DbServiceImpl.class.getName());

    @Autowired
    private ProductDAO productDAO;

    @Autowired
    private DetailDAO detailDAO;

    @Override
    @Transactional
    public void persistToDatabase(Product p, Detail d) {
        try {                        
            productDAO.persistProduct(p);
            detailDAO.persistDetail(d);                     
            
            LOG.info("PERSIST SUCCESSFULLY VIA #persistToDatabase ...");
        } catch (TransactionException e) {
            LOG.error("ERROR TRANSACTION: ", e);
        }
    }

    @Override
    @Transactional(rollbackFor = PersistException.class)
    public void persistToDatabaseWithException(Product p, Detail d) throws PersistException {
        try {
            productDAO.persistProduct(p);
            detailDAO.persistDetail(d);

            LOG.info("THROW A PersistException ...");

            throw new PersistException();
            
        } catch (TransactionException e) {
            LOG.error("ERROR TRANSACTION: ", e);
        }
    }

}
