package be.kuleuven.cs.chikwadraat.socialfridge.servlet;

import com.google.api.server.spi.ServiceException;
import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import be.kuleuven.cs.chikwadraat.socialfridge.DishEndpoint;
import be.kuleuven.cs.chikwadraat.socialfridge.model.Dish;

import static be.kuleuven.cs.chikwadraat.socialfridge.OfyService.ofy;

/**
 * Created by Mattias on 20/04/2014.
 */
public class UpdateDishServlet extends HttpServlet {

    private BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        long dishID = Long.parseLong(req.getParameter("dishID"));
        Dish dish;
        try {
            dish = new DishEndpoint().getDish(dishID);
            req.setAttribute("dish", dish);
        } catch (ServiceException e) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        String submitURL = req.getRequestURI() + "?dishID=" + dishID;
        String formUrl = blobstoreService.createUploadUrl(submitURL);
        req.setAttribute("formURL", formUrl);

        getServletContext().getRequestDispatcher("/WEB-INF/admin/updateDish.jsp").include(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        long dishID = Long.parseLong(req.getParameter("dishID"));
        String dishName = req.getParameter("dishName");
        List<BlobKey> blobs = blobstoreService.getUploads(req).get("dishPicture");
        String action = req.getParameter("action");

        try {
            if(action.equals("update")) {
                // Update dish
                Dish dish = new Dish(dishID);
                dish.setName(dishName);
                if (blobs != null && !blobs.isEmpty()) {
                    // Use first upload as picture
                    BlobKey pictureKey = blobs.remove(0);
                    dish.setPictureKey(pictureKey);
                }
                new DishEndpoint().updateDish(dish);
                resp.sendRedirect("/admin/dishes?updated=" + dishID);
            } else if(action.equals("delete")) {
                // Delete dish
                new DishEndpoint().removeDish(dishID);
                resp.sendRedirect("/admin/dishes?deleted=" + dishID);
            }
        } catch (ServiceException e) {
            throw new ServletException(e);
        } finally {
            // Remove any leftover uploads
            if(blobs != null && !blobs.isEmpty()) {
                blobstoreService.delete(blobs.toArray(new BlobKey[blobs.size()]));
            }
        }
    }

}
