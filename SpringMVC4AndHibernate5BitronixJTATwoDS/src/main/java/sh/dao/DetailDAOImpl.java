package sh.dao;

import sh.model.detail.Detail;

/**
 *
 * @author Anghel Leonard
 */
public class DetailDAOImpl extends GenericDAOImpl<Detail, Long> implements DetailDAO {

    public DetailDAOImpl() {
        super(Detail.class);
    }
}
