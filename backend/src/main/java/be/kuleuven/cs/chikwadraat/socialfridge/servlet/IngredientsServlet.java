package be.kuleuven.cs.chikwadraat.socialfridge.servlet;

import java.io.IOException;
import java.util.Collection;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import be.kuleuven.cs.chikwadraat.socialfridge.IngredientDAO;
import be.kuleuven.cs.chikwadraat.socialfridge.model.Ingredient;

/**
 * Created by Mattias on 06/05/2014.
 */
public class IngredientsServlet extends HttpServlet {

    private IngredientDAO dao = new IngredientDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Collection<Ingredient> ingredients = dao.getIngredients();
        req.setAttribute("ingredients", ingredients);

        getServletContext().getRequestDispatcher("/WEB-INF/admin/ingredients.jsp").include(req, resp);
    }

}
