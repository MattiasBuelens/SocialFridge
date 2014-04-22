package be.kuleuven.cs.chikwadraat.socialfridge.servlet;

import com.google.api.server.spi.ServiceException;

import java.io.IOException;
import java.util.Collection;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import be.kuleuven.cs.chikwadraat.socialfridge.DishDAO;
import be.kuleuven.cs.chikwadraat.socialfridge.model.Dish;

/**
 * Created by Mattias on 22/04/2014.
 */
public class DishesServlet extends HttpServlet {

    private DishDAO dao = new DishDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            Collection<Dish> dishes = dao.getDishes().getItems();
            req.setAttribute("dishes", dishes);
        } catch (ServiceException e) {
            throw new ServletException(e);
        }

        getServletContext().getRequestDispatcher("/WEB-INF/admin/dishes.jsp").include(req, resp);
    }

}
