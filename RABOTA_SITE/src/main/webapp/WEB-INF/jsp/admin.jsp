<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<%@ page language="java" contentType="text/html; charset=utf-8" pageEncoding="utf-8" %>

<!DOCTYPE html>
<html lang="ru" xmlns:th="http://www.w3.org/1999/xhtml">
<head>
  <meta charset="utf-8">
  <title>Список пользователей</title>
  <link rel="stylesheet" type="text/css" href="${contextPath}/resources/css/style.css">
</head>

<body>
<div>
  <table>
    <thead>
    <sec:authorize access="hasRole('ROLE_ADMIN')">
        <th>ID</th>
    </sec:authorize>

    <th>NickName</th>
    <th>UserName</th>

    <sec:authorize access="hasRole('ROLE_ADMIN')">
        <th>Password</th>
    </sec:authorize>

    <th>Roles</th>

    <sec:authorize access="hasRole('ROLE_ADMIN')">
        <th>Delete</th>
    </sec:authorize>

    <sec:authorize access="hasAnyRole('ROLE_ADMIN','ROLE_MODERATOR')">
            <th>Update</th>
        </sec:authorize>
    </thead>

    <c:forEach items="${allUsers}" var="user">
      <tr>
        <sec:authorize access="hasRole('ROLE_ADMIN')">
            <td>${user.id}</td>
        </sec:authorize>

        <td>${user.nickname}</td>
        <td>${user.username}</td>

        <sec:authorize access="hasRole('ROLE_ADMIN')">
            <td>${user.password}</td>
        </sec:authorize>

        <td>
          <c:forEach items="${user.roles}" var="role">${role.name}; </c:forEach>
        </td>

        <sec:authorize access="hasRole('ROLE_ADMIN')">
            <td>
              <form action="${pageContext.request.contextPath}/admin" method="post">
                <input type="hidden" name="userId" value="${user.id}"/>
                <input type="hidden" name="action" value="delete"/>
                <button type="submit">Delete</button>
              </form>
            </td>
        </sec:authorize>

        <sec:authorize access="hasAnyRole('ROLE_ADMIN','ROLE_MODERATOR')">
            <td>
              <form action="${pageContext.request.contextPath}/admin" method="post">
                <input type="hidden" name="userId" value="${user.id}"/>
                <input type="hidden" name="action" value="update"/>
                <button type="submit">Update</button>
              </form>
            </td>
        </sec:authorize>

      </tr>
    </c:forEach>
  </table>
  <a href="/">Главная</a>
</div>
</body>
</html>