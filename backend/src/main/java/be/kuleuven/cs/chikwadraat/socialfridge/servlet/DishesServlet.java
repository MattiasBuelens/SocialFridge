package be.kuleuven.cs.chikwadraat.socialfridge.servlet;

import com.google.api.server.spi.ServiceException;
import com.google.appengine.repackaged.com.google.common.collect.ImmutableList;

import java.io.IOException;
import java.util.Collection;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import be.kuleuven.cs.chikwadraat.socialfridge.DishEndpoint;
import be.kuleuven.cs.chikwadraat.socialfridge.model.Dish;

/**
 * Created by Mattias on 22/04/2014.
 */
public class DishesServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            Collection<Dish> dishes = new DishEndpoint().getDishes().getItems();
            req.setAttribute("dishes", ImmutableList.copyOf(dishes));
        } catch (ServiceException e) {
            throw new ServletException(e);
        }

        getServletContext().getRequestDispatcher("/WEB-INF/admin/dishes.jsp").include(req, resp);
    }

}
