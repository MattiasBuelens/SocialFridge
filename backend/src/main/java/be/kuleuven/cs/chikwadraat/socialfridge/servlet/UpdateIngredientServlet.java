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
public class UpdateIngredientServlet extends HttpServlet {

    private IngredientDAO dao = new IngredientDAO();
    private BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        long ingredientID = Long.parseLong(req.getParameter("ingredientID"));
        Ingredient ingredient = dao.getIngredient(ingredientID);
        if (ingredient == null) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        req.setAttribute("ingredient", ingredient);
        req.setAttribute("categories", Ingredient.Category.values());
        req.setAttribute("units", Unit.values());

        String submitURL = req.getRequestURI() + "?ingredientID=" + ingredientID;
        String formUrl = blobstoreService.createUploadUrl(submitURL);
        req.setAttribute("formURL", formUrl);

        getServletContext().getRequestDispatcher("/WEB-INF/admin/updateIngredient.jsp").include(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        long ingredientID = Long.parseLong(req.getParameter("ingredientID"));
        String name = req.getParameter("ingredientName");
        Ingredient.Category category = Ingredient.Category.valueOf(req.getParameter("ingredientCategory"));
        double defaultAmount = Double.parseDouble(req.getParameter("ingredientDefaultAmount"));
        Unit defaultUnit = Unit.valueOf(req.getParameter("ingredientDefaultUnit"));
        List<BlobKey> blobs = blobstoreService.getUploads(req).get("ingredientPicture");
        String action = req.getParameter("action");

        try {
            if (action.equals("update")) {
                // Update ingredient
                Ingredient ingredient = new Ingredient(ingredientID);
                ingredient.setName(name);
                ingredient.setCategory(category);
                ingredient.setDefaultMeasure(new Measure(defaultAmount, defaultUnit));
                if (blobs != null && !blobs.isEmpty()) {
                    // Use first upload as picture
                    BlobKey pictureKey = blobs.remove(0);
                    ingredient.setPictureKey(pictureKey);
                }
                dao.updateIngredient(ingredient);
                resp.sendRedirect("/admin/ingredients?updated=" + ingredientID);
            } else if (action.equals("delete")) {
                // Delete ingredient
                dao.removeIngredient(Ingredient.getRef(ingredientID));
                resp.sendRedirect("/admin/ingredients?deleted=" + ingredientID);
            }
        } finally {
            // Remove any leftover uploads
            if (blobs != null && !blobs.isEmpty()) {
                blobstoreService.delete(blobs.toArray(new BlobKey[blobs.size()]));
            }
        }
    }

}
