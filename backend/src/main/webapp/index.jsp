<!DOCTYPE html>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html lang="en">
<head>
    <title>Social Fridge</title>
    <%@include file="/WEB-INF/includes/head.jsp" %>
</head>
<body role="document">
<div class="container" role="main">
    <div class="jumbotron">
        <h1>
            Social Fridge
            <a href="https://play.google.com/store/apps/details?id=be.kuleuven.cs.chikwadraat.socialfridge">
                <img src="https://developer.android.com/images/brand/en_generic_rgb_wo_45.png"
                     alt="Get it on Google Play"/>
            </a>
        </h1>

        <p>
            <a href="http://chikwadraat.wordpress.com/" class="btn btn-lg btn-default">
                <span class="glyphicon glyphicon-home"></span> Blog
            </a>
            <a href="/admin/dishes" class="btn btn-lg btn-default">
                <span class="glyphicon glyphicon-cog"></span> Admin
            </a>
        </p>
    </div>
</div>
</body>
</html>