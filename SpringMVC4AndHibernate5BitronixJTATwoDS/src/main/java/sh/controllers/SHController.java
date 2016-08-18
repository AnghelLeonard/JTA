package sh.controllers;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import sh.exception.PersistException;
import sh.model.detail.Detail;
import sh.model.product.Product;
import sh.service.DbService;

/**
 *
 * @author Anghel Leonard
 */
@Controller
@RequestMapping("/")
public class SHController {

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(SHController.class.getName());

    @Autowired
    private DbService dbService;

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String indexPage() {
        return "home";
    }

    @RequestMapping(value = "/store", method = RequestMethod.GET)
    public String productToDatabaseAction() {

        Product p1 = new Product();
        p1.setName("T-Shirt");
        p1.setCode("001A");

        Detail d1 = new Detail();
        d1.setColor("Red");
        d1.setSize("M");
        
        Product p2 = new Product();
        p2.setName("Shoes");
        p2.setCode("002A");

        Detail d2 = new Detail();
        d2.setColor("Blue");
        d2.setSize("23A");

        dbService.persistToDatabase(p1, d1);
        try {
            dbService.persistToDatabaseWithException(p2, d2);
        } catch (PersistException ex) {
            LOG.error("Ops: " + ex);
        }

        return "home";
    }
}
