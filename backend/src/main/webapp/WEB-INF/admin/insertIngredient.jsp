<!DOCTYPE html>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html lang="en">
<head>
    <title>Add new ingredient</title>
    <%@ include file="/WEB-INF/includes/head.jsp" %>
</head>
<body role="document">
<div class="container" role="main">
    <%@ include file="/WEB-INF/includes/admin-nav.jsp" %>
    <div class="page-header">
        <h1>Add new ingredient</h1>
    </div>
    <form role="form" class="form-horizontal" action="${formURL}" method="post"
          enctype="multipart/form-data">
        <div class="form-group">
            <label for="ingredientName" class="col-sm-2 control-label">Name:</label>

            <div class="col-sm-10">
                <input type="text" id="ingredientName" name="ingredientName" class="form-control"/>
            </div>
        </div>
        <div class="form-group">
            <label for="ingredientCategory" class="col-sm-2 control-label">Category:</label>

            <div class="col-sm-10">
                <select id="ingredientCategory" name="ingredientCategory" class="form-control">
                    <c:forEach var="category" items="${categories}">
                        <option value="${category.name}">
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
                <input type="number" min="0" step="any"
                       id="ingredientDefaultAmount"
                       name="ingredientDefaultAmount"
                       class="form-control"/>
            </div>
            <div class="col-sm-2">
                <select id="ingredientDefaultUnit" name="ingredientDefaultUnit"
                        class="form-control">
                    <c:forEach var="unit" items="${units}">
                        <option value="${unit.name}">
                            <c:out value="${unit.label}"/>
                        </option>
                    </c:forEach>
                </select>
            </div>
        </div>
        <div class="form-group">
            <label for="ingredientPicture" class="col-sm-2 control-label">Picture:</label>

            <div class="col-sm-10">
                <input type="file" id="ingredientPicture" name="ingredientPicture"/>
            </div>
        </div>
        <div class="form-group">
            <div class="col-sm-offset-2 col-sm-10">
                <button type="submit" class="btn btn-primary">
                    <span class="glyphicon glyphicon-ok"></span> Add
                </button>
                <a href="/admin/ingredients" class="btn btn-default">Cancel</a>
            </div>
        </div>
    </form>
</div>
</body>
</html>
