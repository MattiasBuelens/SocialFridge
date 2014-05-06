<!DOCTYPE html>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html lang="en">
<head>
    <title>Ingredients</title>
    <%@ include file="/WEB-INF/includes/head.jsp" %>
</head>
<body role="document">
<div class="container" role="main">
    <%@ include file="/WEB-INF/includes/admin-nav.jsp" %>
    <div class="page-header">
        <h1>
            Ingredients
            <a class="btn btn-primary" href="/admin/insertIngredient">
                <span class="glyphicon glyphicon-plus"></span> Add
            </a>
        </h1>
    </div>

    <c:if test="${!empty param.inserted}">
        <div class="alert alert-info alert-dismissable">
            <button type="button" class="close" data-dismiss="alert"
                    aria-hidden="true">&times;</button>
            Added ingredient #
            <c:out value="${param.inserted}"/>
        </div>
    </c:if>
    <c:if test="${!empty param.updated}">
        <div class="alert alert-info alert-dismissable">
            <button type="button" class="close" data-dismiss="alert"
                    aria-hidden="true">&times;</button>
            Updated ingredient #
            <c:out value="${param.updated}"/>
        </div>
    </c:if>
    <c:if test="${!empty param.deleted}">
        <div class="alert alert-info alert-dismissable">
            <button type="button" class="close" data-dismiss="alert"
                    aria-hidden="true">&times;</button>
            Deleted ingredient #
            <c:out value="${param.deleted}"/>
        </div>
    </c:if>

    <table class="table table-hover">
        <thead>
        <tr>
            <th>&nbsp;</th>
            <th style="width: 100%">Name</th>
            <th>Category</th>
            <th>Actions</th>
        </tr>
        </thead>
        <tbody>
        <c:forEach var="ingredient" items="${ingredients}">
            <tr>
                <td>
                    <img src="${ingredient.thumbnailURL}" alt="${ingredient.name}" width="64"/>
                </td>
                <td>
                    <c:out value="${ingredient.name}"/>
                </td>
                <td>
                    <c:out value="${ingredient.category.label}"/>
                </td>
                <td>
                    <a class="btn btn-default"
                       href="/admin/updateIngredient?ingredientID=${ingredient.ID}">
                        <span class="glyphicon glyphicon-pencil"></span> Edit
                    </a>
                </td>
            </tr>
        </c:forEach>
        </tbody>
    </table>
</div>
</body>
</html>
