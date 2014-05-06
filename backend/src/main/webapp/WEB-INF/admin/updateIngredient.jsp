<!DOCTYPE html>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html lang="en">
<head>
    <title>Edit ingredient</title>
    <%@ include file="/WEB-INF/includes/head.jsp" %>
</head>
<body role="document">
<div class="container" role="main">
    <%@ include file="/WEB-INF/includes/admin-nav.jsp" %>
    <div class="page-header">
        <h1>Edit ingredient</h1>
    </div>
    <form role="form" class="form-horizontal" action="${formURL}" method="post"
          enctype="multipart/form-data">
        <div class="form-group">
            <label class="col-sm-2 control-label">ID:</label>

            <div class="col-sm-10">
                <p class="form-control">
                    <c:out value="${ingredient.ID}"/>
                </p>
            </div>
        </div>
        <div class="form-group">
            <label for="ingredientName" class="col-sm-2 control-label">Name:</label>

            <div class="col-sm-10">
                <input type="text" id="ingredientName" name="ingredientName"
                       value="${ingredient.name}"
                       class="form-control"/>
            </div>
        </div>
        <div class="form-group">
            <label for="ingredientCategory" class="col-sm-2 control-label">Category:</label>

            <div class="col-sm-10">
                <select id="ingredientCategory" name="ingredientCategory" class="form-control">
                    <c:forEach var="category" items="${categories}">
                        <option value="${category.name}"
                        ${category == ingredient.category ? "selected" : ""}>
                        <c:out value="${category.label}"/>
                        </option>
                    </c:forEach>
                </select>
            </div>
        </div>
        <div class="form-group">
            <label for="ingredientDefaultAmount" class="col-sm-2 control-label">Default
                amount:</label>

            <div class="col-sm-8">
                <input type="number" id="ingredientDefaultAmount" name="ingredientDefaultAmount"
                       value="${ingredient.defaultAmount}"
                       class="form-control"/>
            </div>
            <div class="col-sm-2">
                <select id="ingredientDefaultUnit" name="ingredientDefaultUnit"
                        class="form-control">
                    <c:forEach var="unit" items="${units}">
                        <option value="${unit.name}"
                        ${unit == ingredient.defaultUnit ? "selected" : ""}>
                        <c:out value="${unit.label}"/>
                        </option>
                    </c:forEach>
                </select>
            </div>
        </div>
        <div class="form-group">
            <label for="ingredientPicture" class="col-sm-2 control-label">Picture:</label>

            <div class="col-sm-10">
                <img src="${ingredient.thumbnailURL}" alt="${ingredient.name}" class="img-thumbnail"
                     width="128"/>

                <p>
                    <small>Upload a new picture if you want to replace the current.</small>
                </p>
                <input type="file" id="ingredientPicture" name="ingredientPicture"/>
            </div>
        </div>
        <div class="form-group">
            <div class="col-sm-offset-2 col-sm-10">
                <button type="submit" name="action" value="update" class="btn btn-primary">
                    <span class="glyphicon glyphicon-ok"></span> Update
                </button>
                <button type="submit" name="action" value="delete" class="btn btn-danger">
                    <span class="glyphicon glyphicon-trash"></span> Delete
                </button>
                <a href="/admin/ingredients" class="btn btn-default">Cancel</a>
            </div>
        </div>
    </form>
</div>
</body>
</html>
