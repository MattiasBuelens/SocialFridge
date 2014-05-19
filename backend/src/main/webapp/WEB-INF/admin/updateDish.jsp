<!DOCTYPE html>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html lang="en">
<head>
    <title>Edit dish</title>
    <%@ include file="/WEB-INF/includes/head.jsp" %>
</head>
<body role="document">
<div class="container" role="main">
    <%@ include file="/WEB-INF/includes/admin-nav.jsp" %>
    <div class="page-header">
        <h1>Edit dish</h1>
    </div>
    <form role="form" class="form-horizontal" action="${formURL}" method="post"
          enctype="multipart/form-data">
        <div class="form-group">
            <label class="col-sm-2 control-label">ID:</label>

            <div class="col-sm-10">
                <p class="form-control">
                    <c:out value="${dish.ID}"/>
                </p>
            </div>
        </div>
        <div class="form-group">
            <label for="dishName" class="col-sm-2 control-label">Name:</label>

            <div class="col-sm-10">
                <input type="text" id="dishName" name="dishName" value="${dish.name}"
                       class="form-control"/>
            </div>
        </div>
        <div class="form-group">
            <label for="dishPicture" class="col-sm-2 control-label">Picture:</label>

            <div class="col-sm-10">
                <img src="${dish.thumbnailURL}" alt="${dish.name}" class="img-thumbnail"
                     width="128"/>

                <p>
                    <small>Upload a new picture if you want to replace the current.</small>
                </p>
                <input type="file" id="dishPicture" name="dishPicture"/>
            </div>
        </div>
        <div class="form-group">
            <label for="dishAddItem" class="col-sm-2 control-label">Items:</label>

            <div class="col-sm-10">
                <table class="table">
                    <thead>
                    <tr>
                        <th>&nbsp;</th>
                        <th>Name</th>
                        <th colspan="2">Amount (per person)</th>
                        <th>&nbsp;</th>
                    </tr>
                    </thead>
                    <tbody>
                    <c:forEach var="item" varStatus="loopStatus" items="${dish.items}">
                        <tr>
                            <td>
                                <img src="${item.ingredient.thumbnailURL}"
                                     alt="${item.ingredient.name}"
                                     width="32" class="img-thumbnail"/></td>
                            <td>
                                <c:out value="${item.ingredient.name}"/>
                            </td>
                            <td>
                                <input type="number" min="0"
                                       id="dishItems[${loopStatus.index}].amount"
                                       name="dishItems[${loopStatus.index}].amount"
                                       value="${item.standardAmount}" class="form-control"/></td>
                            <td>
                                <c:out value="${item.standardUnit.label}"/>
                            </td>
                            <td>
                                <button type="submit" name="action"
                                        value="dishItems[${loopStatus.index}].delete"
                                        class="btn btn-danger">
                                    <span class="glyphicon glyphicon-trash"></span> Delete
                                </button>
                            </td>
                        </tr>
                    </c:forEach>
                    </tbody>
                </table>
            </div>
        </div>
        <div class="form-group">
            <div class="col-sm-offset-2 col-sm-10">
                <div class="col-sm-8">
                    <select id="dishAddItem" name="dishAddItem"
                            class="form-control">
                        <option value="">---</option>
                        <c:forEach var="ingredient" items="${ingredients}">
                            <option value="${ingredient.ID}">
                                <c:out value="${ingredient.name}"/>
                            </option>
                        </c:forEach>
                    </select>
                </div>
                <div class="col-sm-2">
                    <button type="submit" name="action" value="addItem" class="btn">
                        <span class="glyphicon glyphicon-plus"></span> Add
                    </button>
                </div>
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
                <a href="/admin/dishes" class="btn btn-default">Cancel</a>
            </div>
        </div>
    </form>
</div>
</body>
</html>
