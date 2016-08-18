package sh.dao;

import java.io.Serializable;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 *
 * @author Anghel Leonard
 */
@Repository
@Transactional
public class GenericDAOImpl<T, ID extends Serializable> implements GenericDAO<T, ID> {

    @PersistenceContext(unitName = "productPersistenceUnit")
    private EntityManager entityManagerProduct;

    @PersistenceContext(unitName = "detailPersistenceUnit")
    private EntityManager entityManagerDetail;

    private final Class<T> entityClass;

    public EntityManager getEntityManageProduct() {
        return entityManagerProduct;
    }

    public EntityManager getEntityManageDetail() {
        return entityManagerDetail;
    }

    protected GenericDAOImpl(Class<T> entityClass) {
        this.entityClass = entityClass;
    }

    @Override
    public T persistProduct(T entity) {
        entityManagerProduct.persist(entity);
        return entity;
    }

    @Override
    public T persistDetail(T entity) {
        entityManagerDetail.persist(entity);
        return entity;
    }
}
