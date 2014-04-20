<!DOCTYPE html>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html lang="en">
<head>
    <title>Dishes</title>
    <%@include file="/WEB-INF/includes/head.jsp" %>
</head>
<body role="document">
<div class="container" role="main">
    <div class="page-header">
        <h1>
            Dishes
            <a class="btn btn-primary" href="/admin/insertDish">
                <span class="glyphicon glyphicon-plus"></span> Add
            </a>
        </h1>
    </div>

    <c:if test="${!empty param.inserted}">
        <div class="alert alert-info alert-dismissable">
            <button type="button" class="close" data-dismiss="alert"
                    aria-hidden="true">&times;</button>
            Added dish #
            <c:out value="${param.inserted}"/>
        </div>
    </c:if>
    <c:if test="${!empty param.updated}">
        <div class="alert alert-info alert-dismissable">
            <button type="button" class="close" data-dismiss="alert"
                    aria-hidden="true">&times;</button>
            Updated dish #
            <c:out value="${param.updated}"/>
        </div>
    </c:if>
    <c:if test="${!empty param.deleted}">
        <div class="alert alert-info alert-dismissable">
            <button type="button" class="close" data-dismiss="alert"
                    aria-hidden="true">&times;</button>
            Deleted dish #
            <c:out value="${param.deleted}"/>
        </div>
    </c:if>

    <table class="table table-hover">
        <thead>
        <tr>
            <th>&nbsp;</th>
            <th style="width: 100%">Name</th>
            <th>Actions</th>
        </tr>
        </thead>
        <tbody>
        <c:forEach var="dish" items="${dishes}">
            <tr>
                <td>
                    <img src="${dish.thumbnailURL}" alt="${dish.name}" width="64"/>
                </td>
                <td>
                    <c:out value="${dish.name}"/>
                </td>
                <td>
                    <a class="btn btn-default" href="/admin/updateDish?dishID=${dish.ID}">
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
