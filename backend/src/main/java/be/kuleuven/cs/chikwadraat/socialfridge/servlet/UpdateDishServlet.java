package be.kuleuven.cs.chikwadraat.socialfridge.servlet;

import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.google.common.base.Strings;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import be.kuleuven.cs.chikwadraat.socialfridge.DishDAO;
import be.kuleuven.cs.chikwadraat.socialfridge.IngredientDAO;
import be.kuleuven.cs.chikwadraat.socialfridge.model.Dish;
import be.kuleuven.cs.chikwadraat.socialfridge.model.DishItem;
import be.kuleuven.cs.chikwadraat.socialfridge.model.Ingredient;

/**
 * Created by Mattias on 20/04/2014.
 */
public class UpdateDishServlet extends HttpServlet {

    private DishDAO dao = new DishDAO();
    private IngredientDAO ingredientDAO = new IngredientDAO();
    private BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        long dishID = Long.parseLong(req.getParameter("dishID"));
        Dish dish = dao.getDish(dishID);
        if (dish == null) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        req.setAttribute("dish", dish);
        req.setAttribute("ingredients", ingredientDAO.getIngredients());

        String submitURL = req.getRequestURI() + "?dishID=" + dishID;
        String formUrl = blobstoreService.createUploadUrl(submitURL);
        req.setAttribute("formURL", formUrl);

        ServletUtils.disableCaching(resp);
        getServletContext().getRequestDispatcher("/WEB-INF/admin/updateDish.jsp").include(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        long dishID = Long.parseLong(req.getParameter("dishID"));
        String name = req.getParameter("dishName");
        List<BlobKey> blobs = blobstoreService.getUploads(req).get("dishPicture");
        String action = req.getParameter("action");
        String addIngredientID = req.getParameter("dishAddItem");
        String redirectURL = "/admin/dishes?updated=";

        // Update items
        List<DishItem> items = new ArrayList<DishItem>();
        for (int i = 0; !Strings.isNullOrEmpty(req.getParameter("dishItems[" + i + "].ingredient")); i++) {
            long ingredientID = Long.parseLong(req.getParameter("dishItems[" + i + "].ingredient"));
            double standardAmount = Double.parseDouble(req.getParameter("dishItems[" + i + "].amount"));
            DishItem item = new DishItem(Ingredient.getRef(ingredientID));
            item.setStandardAmount(standardAmount);
            items.add(item);
        }
        // Delete item
        Matcher m = Pattern.compile("^dishItems\\[(\\d+)\\]\\.delete$").matcher(action);
        if (m.matches()) {
            int index = Integer.parseInt(m.group(1));
            items.remove(index);
            redirectURL = "/admin/updateDish?dishID=";
        }
        // Add new item
        if (action.equals("addItem") && !Strings.isNullOrEmpty(addIngredientID)) {
            long ingredientID = Long.parseLong(addIngredientID);
            DishItem item = new DishItem(Ingredient.getRef(ingredientID));
            items.add(item);
            redirectURL = "/admin/updateDish?dishID=";
        }

        try {
            if (action.equals("delete")) {
                // Delete dish
                dao.removeDish(Dish.getRef(dishID));
                redirectURL = "/admin/dishes?deleted=";
            } else {
                // Update dish
                Dish dish = new Dish(dishID);
                dish.setName(name);
                dish.setItems(items);
                if (blobs != null && !blobs.isEmpty()) {
                    // Use first upload as picture
                    dish.setPictureKey(blobs.remove(0));
                }
                dao.updateDish(dish);
            }
            resp.sendRedirect(redirectURL + dishID);
        } finally {
            // Remove any leftover uploads
            if (blobs != null && !blobs.isEmpty()) {
                blobstoreService.delete(blobs.toArray(new BlobKey[blobs.size()]));
            }
        }
    }

}
