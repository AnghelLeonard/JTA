package sh.service;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;
import sh.dao.ProductDAO;
import sh.model.Product;

/**
 *
 * @author Anghel Leonard
 */
@Repository
public class ProductServiceImpl implements ProductService {

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(ProductServiceImpl.class.getName());

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Autowired
    private ProductDAO productDAO;

    @Override
    public void productToDatabaseTransactionTemplate() {
        try {
            transactionTemplate.execute((TransactionCallback<Void>) transactionStatus -> {
                Product p = new Product();
                p.setName("T-Shirt");
                p.setCode("0448FFD-X");

                productDAO.persist(p);
                return null;
            });
        } catch (TransactionException e) {
            LOG.error("ERROR TRANSACTION: ", e);
        }

        LOG.info("PERSIST SUCCESSFULLY VIA #transactionTemplate ...");
    }

    @Override
    @Transactional
    public void productToDatabaseTransactional() {
        try {
            Product p = new Product();
            p.setName("Shoes");
            p.setCode("04eef4-XY");

            productDAO.persist(p);
        } catch (TransactionException e) {
            LOG.error("ERROR TRANSACTION: ", e);
        }

        LOG.info("PERSIST SUCCESSFULLY via Transactional ...");
    }
}
