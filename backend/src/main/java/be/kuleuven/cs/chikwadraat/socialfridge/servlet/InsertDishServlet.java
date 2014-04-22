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

import be.kuleuven.cs.chikwadraat.socialfridge.DishDAO;
import be.kuleuven.cs.chikwadraat.socialfridge.model.Dish;

/**
 * Created by Mattias on 20/04/2014.
 */
public class InsertDishServlet extends HttpServlet {

    private DishDAO dao = new DishDAO();
    private BlobstoreService blobstoreService = BlobstoreServiceFactory.getBlobstoreService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String formUrl = blobstoreService.createUploadUrl(req.getRequestURI());
        req.setAttribute("formURL", formUrl);

        getServletContext().getRequestDispatcher("/WEB-INF/admin/insertDish.jsp").include(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String dishName = req.getParameter("dishName");
        List<BlobKey> blobs = blobstoreService.getUploads(req).get("dishPicture");

        try {
            // Insert dish
            Dish dish = new Dish();
            dish.setName(dishName);
            if (blobs == null || blobs.isEmpty()) {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }
            dish.setPictureKey(blobs.remove(0));
            dish = dao.updateDish(dish);

            long dishID = dish.getID();
            resp.sendRedirect("/admin/dishes?inserted=" + dishID);
        } catch (ServiceException e) {
            throw new ServletException(e);
        } finally {
            // Remove any leftover uploads
            if (blobs != null && !blobs.isEmpty()) {
                blobstoreService.delete(blobs.toArray(new BlobKey[blobs.size()]));
            }
        }
    }

}
