package be.kuleuven.cs.chikwadraat.socialfridge.servlet;

import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import be.kuleuven.cs.chikwadraat.socialfridge.IngredientDAO;
import be.kuleuven.cs.chikwadraat.socialfridge.measuring.Measure;
import be.kuleuven.cs.chikwadraat.socialfridge.measuring.Unit;
import be.kuleuven.cs.chikwadraat.socialfridge.model.Ingredient;

/**
 * Created by Mattias on 06/05/2014.
 */
public class InsertIngredientServlet extends HttpServlet {

    private IngredientDAO dao = new IngredientDAO();
    private BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String formUrl = blobstoreService.createUploadUrl(req.getRequestURI());
        req.setAttribute("formURL", formUrl);
        req.setAttribute("categories", Ingredient.Category.values());
        req.setAttribute("units", Unit.values());

        getServletContext().getRequestDispatcher("/WEB-INF/admin/insertIngredient.jsp").include(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String name = req.getParameter("ingredientName");
        Ingredient.Category category = Ingredient.Category.valueOf(req.getParameter("ingredientCategory"));
        double defaultAmount = Double.parseDouble(req.getParameter("ingredientDefaultAmount"));
        Unit defaultUnit = Unit.valueOf(req.getParameter("ingredientDefaultUnit"));
        List<BlobKey> blobs = blobstoreService.getUploads(req).get("ingredientPicture");

        try {
            // Insert ingredient
            Ingredient ingredient = new Ingredient(name, category, new Measure(defaultAmount, defaultUnit));
            if (blobs == null || blobs.isEmpty()) {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }
            ingredient.setPictureKey(blobs.remove(0));
            ingredient = dao.updateIngredient(ingredient);

            long ingredientID = ingredient.getID();
            resp.sendRedirect("/admin/ingredients?inserted=" + ingredientID);
        } finally {
            // Remove any leftover uploads
            if (blobs != null && !blobs.isEmpty()) {
                blobstoreService.delete(blobs.toArray(new BlobKey[blobs.size()]));
            }
        }
    }

}
