package be.kuleuven.cs.chikwadraat.socialfridge.servlet;

import com.google.appengine.api.blobstore.BlobInfo;
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
import be.kuleuven.cs.chikwadraat.socialfridge.util.BlobUtils;

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

        ServletUtils.disableCaching(resp);
        getServletContext().getRequestDispatcher("/WEB-INF/admin/insertDish.jsp").include(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        String name = req.getParameter("dishName");
        List<BlobInfo> blobs = blobstoreService.getBlobInfos(req).get("dishPicture");

        try {
            // Insert dish
            Dish dish = new Dish(name);
            if (blobs == null || blobs.isEmpty() || !BlobUtils.isImage(blobs.get(0))) {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }
            dish.setPictureKey(blobs.remove(0).getBlobKey());
            dish = dao.updateDish(dish);

            long dishID = dish.getID();
            resp.sendRedirect("/admin/dishes?inserted=" + dishID);
        } finally {
            // Remove any leftover uploads
            BlobUtils.deleteBlobInfos(blobs);
        }
    }

}
