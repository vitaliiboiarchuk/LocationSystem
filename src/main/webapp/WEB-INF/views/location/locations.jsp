<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
    <title>Title</title>
</head>
<body>
<h3>Added locations</h3>
<table class="table">
    <thead>
    <tr>
        <th scope="col">Name</th>
        <th scope="col">Address</th>
    </tr>
    </thead>
    <tbody>
    <c:forEach items="${locations}" var="location">
        <tr>
            <td>${location.name}</td>
            <td>${location.address}</td>
            <td><a href="<c:url value="/location/${location.id}/"/>">Show friends</a></td>
            <td><a href="<c:url value="/location/${location.id}/delete/"/>">Delete location</a></td>
        </tr>
    </c:forEach>
    </tbody>
</table>
<h3>Shared locations from friends</h3>
<table class="table">
    <thead>
    <tr>
        <th scope="col">Name</th>
        <th scope="col">Address</th>
    </tr>
    </thead>
    <tbody>
    <c:forEach items="${adminAccess}" var="location">
        <tr>
            <td>${location.name}</td>
            <td>${location.address}</td>
            <td>(Admin access)</td>
            <td><a href="<c:url value="/location/${location.id}/"/>">Show friends</a></td>
        </tr>
    </c:forEach>
    <c:forEach items="${readAccess}" var="location">
        <tr>
            <td>${location.name}</td>
            <td>${location.address}</td>
            <td>(Read access)</td>
            <td><a href="<c:url value="/location/${location.id}/"/>">Show friends</a></td>
        </tr>
    </c:forEach>
    </tbody>
</table>
</body>
</html>
